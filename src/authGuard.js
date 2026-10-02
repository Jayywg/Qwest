import { supabase } from "./supabaseClient";

/**
 * Checks if a user session exists using supabase.auth.getSession().
 * If no session is found, redirects to /login.
 * Returns the session object if valid.
 */
export async function requireAuth(redirectPath = "/login") {
  const { data: { session }, error } = await supabase.auth.getSession();

  if (error || !session) {
    window.location.href = redirectPath;
    return null;
  }

  return session;
}
