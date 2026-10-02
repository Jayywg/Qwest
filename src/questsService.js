import { supabase } from "./supabaseClient";
import { uploadQuestAttachment } from "./storageService";
import { isProUser, FREE_PLAN_QUEST_LIMIT } from "./stripeService";

/**
 * Quests / Tasks Supabase Data Service
 * Implements CRUD operations scoped to the authenticated user.
 * Protects Pro paid feature: Unlimited Quests for Pro members, max 3 for Free users.
 */

// 1. Load user's quests
export async function getQuests() {
  const { data, error } = await supabase
    .from("quests")
    .select("*")
    .eq("is_archived", false)
    .order("created_at", { ascending: false });

  if (error) {
    console.error("Error loading quests:", error.message);
    throw error;
  }
  return data || [];
}

// 2. Count active quests
export async function getQuestsCount() {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) return 0;

  const { count, error } = await supabase
    .from("quests")
    .select("*", { count: "exact", head: true })
    .eq("user_id", user.id)
    .eq("is_archived", false);

  if (error) {
    console.error("Error counting quests:", error.message);
    return 0;
  }
  return count || 0;
}

// 3. Create a new quest (with Pro limit check and optional attachment file upload)
export async function createQuest({
  title,
  description = "",
  category = "General",
  difficulty = "NORMAL",
  recurrence = "DAILY",
  attachmentFile = null,
}) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("User must be logged in to create a quest.");

  // Paid Feature Protection:
  // Free users are restricted to FREE_PLAN_QUEST_LIMIT (3). Pro users have unlimited quests.
  const isPro = await isProUser();
  if (!isPro) {
    const activeCount = await getQuestsCount();
    if (activeCount >= FREE_PLAN_QUEST_LIMIT) {
      throw new Error(
        `Free plan limit reached (${FREE_PLAN_QUEST_LIMIT} quests max). Upgrade to Pro to create unlimited quests and unlock pets & items!`
      );
    }
  }

  const { data, error } = await supabase
    .from("quests")
    .insert([
      {
        user_id: user.id,
        title,
        description,
        category,
        difficulty,
        recurrence,
      },
    ])
    .select()
    .single();

  if (error) {
    console.error("Error creating quest:", error.message);
    throw error;
  }

  // If a file was selected during quest creation, upload it now
  if (attachmentFile) {
    try {
      const { filePath } = await uploadQuestAttachment(data.id, attachmentFile);
      data.attachment_path = filePath;
    } catch (uploadErr) {
      console.error("Failed to upload quest attachment:", uploadErr);
    }
  }

  return data;
}

// 4. Update an existing quest
export async function updateQuest(id, updates) {
  const { data, error } = await supabase
    .from("quests")
    .update(updates)
    .eq("id", id)
    .select()
    .single();

  if (error) {
    console.error("Error updating quest:", error.message);
    throw error;
  }
  return data;
}

// 5. Delete a quest
export async function deleteQuest(id) {
  const { error } = await supabase
    .from("quests")
    .delete()
    .eq("id", id);

  if (error) {
    console.error("Error deleting quest:", error.message);
    throw error;
  }
  return true;
}

// 6. Complete a quest (log completion)
export async function completeQuest(questId, expAwarded = 10, goldAwarded = 5) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("User must be logged in.");

  const { data, error } = await supabase
    .from("quest_completions")
    .insert([
      {
        user_id: user.id,
        quest_id: questId,
        date: new Date().toISOString().split("T")[0],
        exp_awarded: expAwarded,
        gold_awarded: goldAwarded,
      },
    ])
    .select()
    .single();

  if (error) {
    console.error("Error recording quest completion:", error.message);
    throw error;
  }
  return data;
}
