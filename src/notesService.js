import { supabase } from "./supabaseClient";

export const FREE_PLAN_NOTE_LIMIT = 3;

/**
 * 1. Fetches current authenticated user and counts notes in "notes" table.
 * Returns exact count for the user.
 */
export async function getNotesCount() {
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    return 0;
  }

  const { count, error } = await supabase
    .from("notes")
    .select("*", { count: "exact", head: true })
    .eq("user_id", user.id);

  if (error) {
    console.error("Error counting notes:", error.message);
    throw error;
  }

  return count || 0;
}

/**
 * Fetches all notes for the authenticated user.
 */
export async function getNotes() {
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) return [];

  const { data, error } = await supabase
    .from("notes")
    .select("*")
    .eq("user_id", user.id)
    .order("created_at", { ascending: false });

  if (error) {
    console.error("Error fetching notes:", error.message);
    throw error;
  }

  return data || [];
}

/**
 * 2. Creates a note with strict SaaS plan limit validation.
 * Ensures users cannot bypass the 3-note limit by calling the create function manually.
 */
export async function createNote({ title, content = "" }) {
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    throw new Error("You must be logged in to create a note.");
  }

  // Count existing notes for the user directly in Supabase
  const currentCount = await getNotesCount();

  // Strict check: if notes count >= 3, block creation
  if (currentCount >= FREE_PLAN_NOTE_LIMIT) {
    throw new Error("Free plan limit reached. Upgrade to Pro to create unlimited notes.");
  }

  const { data, error } = await supabase
    .from("notes")
    .insert([
      {
        user_id: user.id,
        title: title.trim(),
        content: content.trim(),
      },
    ])
    .select()
    .single();

  if (error) {
    console.error("Error creating note:", error.message);
    throw error;
  }

  return data;
}

/**
 * Deletes a note by ID.
 */
export async function deleteNote(noteId) {
  const { error } = await supabase
    .from("notes")
    .delete()
    .eq("id", noteId);

  if (error) {
    console.error("Error deleting note:", error.message);
    throw error;
  }

  return true;
}

/**
 * Real-time subscription to automatically update "Total Notes" count
 * when notes are created, updated, or deleted.
 */
export function subscribeToNotes(userId, onNotesChanged) {
  if (!userId) return null;

  const channel = supabase
    .channel(`public:notes:${userId}`)
    .on(
      "postgres_changes",
      {
        event: "*",
        schema: "public",
        table: "notes",
        filter: `user_id=eq.${userId}`,
      },
      () => {
        if (onNotesChanged) {
          onNotesChanged();
        }
      }
    )
    .subscribe();

  return () => {
    supabase.removeChannel(channel);
  };
}
