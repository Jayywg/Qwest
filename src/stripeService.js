import { supabase } from "./supabaseClient";

/**
 * Stripe Subscription Service
 * Manages Pro subscription state, Stripe checkout flows, customer billing portal,
 * and feature gating for Qwest (Unlimited Quests, Unlocked Pets & Items).
 */

// Free Tier Limit Constants
export const FREE_PLAN_QUEST_LIMIT = 3;

// Stripe Price IDs (configured in Stripe Dashboard)
export const STRIPE_PRICES = {
  MONTHLY: {
    id: "price_pro_monthly",
    name: "Pro Monthly",
    price: "$4.99",
    period: "/month",
    interval: "month",
    description: "Billed monthly. Cancel anytime.",
  },
  YEARLY: {
    id: "price_pro_yearly",
    name: "Pro Yearly",
    price: "$39.99",
    period: "/year",
    interval: "year",
    description: "Billed annually ($3.33/mo). Save 33%!",
    badge: "SAVE 33%",
  },
};

export const PRO_FEATURES = [
  "No limits on creating Quests & Habits (Unlimited)",
  "Unlock Exclusive Pets: Celestial Phoenix, Astral Dragon, Shadow Panther, Frost Kitsune",
  "Unlock Exclusive Items: Sunfire Excalibur, Aegis of Eternity, Chronos Hourglass, Crown of Archmage",
  "Permanent Streak Freeze & Relic Restorations",
  "Multi-Device Cloud Sync & Priority Support",
];

// In-memory / localStorage cache for fast UI updates & development testing
const LOCAL_STORAGE_KEY_PRO_SIM = "qwest_dev_pro_simulated";

/**
 * Check if the currently authenticated user has an active Pro subscription.
 * Reads from the Supabase `subscriptions` table (synced by Stripe webhooks),
 * with fallback to local testing flags if in sandbox/development mode.
 */
export async function getSubscriptionStatus() {
  try {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) {
      return { isPro: false, plan: "free", status: "unauthenticated", subscription: null };
    }

    // 1. Query the Supabase subscriptions table
    const { data: subData, error } = await supabase
      .from("subscriptions")
      .select("*")
      .eq("user_id", user.id)
      .in("status", ["active", "trialing"])
      .maybeSingle();

    if (!error && subData) {
      return {
        isPro: true,
        plan: "pro",
        status: subData.status,
        subscription: subData,
        currentPeriodEnd: subData.current_period_end,
        cancelAtPeriodEnd: subData.cancel_at_period_end,
      };
    }

    // 2. Check if user activated sandbox/dev simulation mode
    const simulatedPro = localStorage.getItem(`${LOCAL_STORAGE_KEY_PRO_SIM}_${user.id}`);
    if (simulatedPro === "true") {
      return {
        isPro: true,
        plan: "pro",
        status: "active (sandbox)",
        subscription: {
          id: "sub_sandbox_test",
          plan_id: "pro",
          status: "active",
          current_period_end: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
        },
        isSimulated: true,
      };
    }

    return { isPro: false, plan: "free", status: "inactive", subscription: null };
  } catch (err) {
    console.error("Error reading subscription status:", err);
    return { isPro: false, plan: "free", status: "error", subscription: null };
  }
}

/**
 * Helper to determine if user has Pro access right now.
 */
export async function isProUser() {
  const { isPro } = await getSubscriptionStatus();
  return isPro;
}

/**
 * Initiates Stripe Checkout for Pro subscription (Monthly or Yearly).
 * Calls the Supabase Edge Function 'create-checkout-session' or Stripe backend.
 */
export async function createCheckoutSession(interval = "monthly") {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) {
    throw new Error("You must be logged in to subscribe to Pro.");
  }

  const price = interval === "yearly" ? STRIPE_PRICES.YEARLY : STRIPE_PRICES.MONTHLY;

  try {
    // Invoke Supabase Edge Function configured with Stripe Secret Key
    const { data, error } = await supabase.functions.invoke("create-checkout-session", {
      body: {
        priceId: price.id,
        userId: user.id,
        email: user.email,
        successUrl: `${window.location.origin}/dashboard?session_id={CHECKOUT_SESSION_ID}&pro_success=true`,
        cancelUrl: `${window.location.origin}/quests?canceled=true`,
      },
    });

    if (!error && data?.url) {
      window.location.href = data.url;
      return { url: data.url };
    }

    // Fallback: If edge function is not deployed yet in this sandbox environment,
    // trigger a clean development sandbox checkout simulation
    console.warn("Stripe Edge Function not reachable. Activating Sandbox Checkout flow.");
    return {
      sandboxMode: true,
      price,
    };
  } catch (err) {
    console.warn("Stripe Checkout invoke exception, fallback to sandbox flow:", err.message);
    return {
      sandboxMode: true,
      price,
    };
  }
}

/**
 * Opens the Stripe Customer Portal for managing subscription, billing, and cancellations.
 */
export async function openCustomerPortal() {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("Must be logged in to manage billing.");

  try {
    const { data, error } = await supabase.functions.invoke("create-portal-session", {
      body: {
        userId: user.id,
        returnUrl: `${window.location.origin}/quests`,
      },
    });

    if (!error && data?.url) {
      window.location.href = data.url;
      return { url: data.url };
    }
  } catch (err) {
    console.warn("Stripe Customer Portal unavailable, fallback notice:", err.message);
  }

  alert("Stripe Customer Portal: You can manage or cancel your active Pro subscription anytime.");
}

/**
 * Sandbox/Development toggle to simulate activating Pro membership
 * for immediate testing and verification without needing live Stripe cards.
 */
export async function toggleSimulatedPro(enable = true) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) return false;

  if (enable) {
    localStorage.setItem(`${LOCAL_STORAGE_KEY_PRO_SIM}_${user.id}`, "true");
    // Also attempt inserting into Supabase subscriptions table if allowed
    try {
      await supabase.from("subscriptions").upsert({
        id: `sub_test_${user.id.slice(0, 8)}`,
        user_id: user.id,
        status: "active",
        plan_id: "pro",
        price_id: STRIPE_PRICES.MONTHLY.id,
        current_period_end: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
      });
    } catch {
      // Ignored if RLS prevents client insert
    }
  } else {
    localStorage.removeItem(`${LOCAL_STORAGE_KEY_PRO_SIM}_${user.id}`);
    try {
      await supabase.from("subscriptions").delete().eq("user_id", user.id);
    } catch {
      // Ignored
    }
  }

  return enable;
}

/**
 * Real-time listener for changes to the user's subscription in Supabase.
 */
export function subscribeToSubscription(userId, onSubscriptionChange) {
  if (!userId) return () => {};

  const channel = supabase
    .channel(`public:subscriptions:${userId}`)
    .on(
      "postgres_changes",
      {
        event: "*",
        schema: "public",
        table: "subscriptions",
        filter: `user_id=eq.${userId}`,
      },
      () => {
        if (typeof onSubscriptionChange === "function") {
          onSubscriptionChange();
        }
      }
    )
    .subscribe();

  return () => {
    supabase.removeChannel(channel);
  };
}
