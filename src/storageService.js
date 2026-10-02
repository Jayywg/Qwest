import { supabase } from "./supabaseClient";

export const BUCKET_NAME = "app-files";

/**
 * Utility to generate a unique random ID (UUID v4 format)
 */
function generateUUID() {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * Uploads a file to the private "app-files" bucket adhering to the folder rule:
 * ${auth.uid()}/${featureName}/${itemId}/${uuid}.${extension}
 */
export async function uploadToStorage({ file, featureName, itemId = "general" }) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("User must be logged in to upload files.");

  const extension = file.name.split(".").pop().toLowerCase();
  const fileUuid = generateUUID();
  const filePath = `${user.id}/${featureName}/${itemId}/${fileUuid}.${extension}`;

  const { data, error } = await supabase.storage
    .from(BUCKET_NAME)
    .upload(filePath, file, {
      cacheControl: "3600",
      upsert: true,
    });

  if (error) {
    console.error("Storage upload error:", error.message);
    throw error;
  }

  return {
    filePath: data.path,
    fullPath: data.fullPath,
  };
}

/**
 * Generates a signed URL for a file in the private bucket.
 * Default expiration is 1 hour (3600 seconds).
 */
export async function getFileSignedUrl(filePath, expiresIn = 3600) {
  if (!filePath) return null;

  const { data, error } = await supabase.storage
    .from(BUCKET_NAME)
    .createSignedUrl(filePath, expiresIn);

  if (error) {
    console.error("Error creating signed URL:", error.message);
    return null;
  }

  return data?.signedUrl || null;
}

/**
 * Deletes a file from the private Storage bucket.
 */
export async function deleteFromStorage(filePath) {
  if (!filePath) return true;

  const { error } = await supabase.storage
    .from(BUCKET_NAME)
    .remove([filePath]);

  if (error) {
    console.error("Error deleting file from storage:", error.message);
    throw error;
  }

  return true;
}

// ============================================================================
// FEATURE-SPECIFIC ATTACHMENT & AVATAR HANDLERS
// ============================================================================

/**
 * 1. AVATAR UPLOAD & DELETE
 * Saved to: ${auth.uid()}/avatars/profile/${uuid}.${extension}
 * Database: profiles table (avatar_path column)
 */
export async function uploadUserAvatar(file) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("Not logged in");

  // Upload file to storage
  const { filePath } = await uploadToStorage({
    file,
    featureName: "avatars",
    itemId: "profile",
  });

  // Update profile in database
  const { error: dbError } = await supabase
    .from("profiles")
    .update({
      avatar_path: filePath,
      updated_at: new Date().toISOString(),
    })
    .eq("id", user.id);

  if (dbError) throw dbError;

  // Generate signed URL for immediate display
  const signedUrl = await getFileSignedUrl(filePath);

  return { filePath, signedUrl };
}

export async function deleteUserAvatar(currentPath) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("Not logged in");

  // Remove from storage
  if (currentPath) {
    await deleteFromStorage(currentPath);
  }

  // Clear reference in database
  const { error: dbError } = await supabase
    .from("profiles")
    .update({
      avatar_path: null,
      updated_at: new Date().toISOString(),
    })
    .eq("id", user.id);

  if (dbError) throw dbError;
  return true;
}

/**
 * 2. QUEST ATTACHMENT UPLOAD & DELETE
 * Saved to: ${auth.uid()}/quests/${questId}/${uuid}.${extension}
 * Database: quests table (attachment_path column)
 */
export async function uploadQuestAttachment(questId, file) {
  const { filePath } = await uploadToStorage({
    file,
    featureName: "quests",
    itemId: questId,
  });

  // Update quest record in database
  const { error: dbError } = await supabase
    .from("quests")
    .update({ attachment_path: filePath })
    .eq("id", questId);

  if (dbError) throw dbError;

  const signedUrl = await getFileSignedUrl(filePath);
  return { filePath, signedUrl };
}

export async function deleteQuestAttachment(questId, attachmentPath) {
  // Remove from storage
  if (attachmentPath) {
    await deleteFromStorage(attachmentPath);
  }

  // Clear reference in database
  const { error: dbError } = await supabase
    .from("quests")
    .update({ attachment_path: null })
    .eq("id", questId);

  if (dbError) throw dbError;
  return true;
}

/**
 * 3. GENERAL USER FILE MANAGER (Quests, Progression, Documents)
 * Saved to: ${auth.uid()}/${category}/${itemId}/${uuid}.${extension}
 * Database: user_files table (with RLS ensuring user only sees their own files)
 */
export async function uploadUserFile({ file, category = "progression", itemId = "general" }) {
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) throw new Error("User must be logged in to upload files.");

  // Upload to Supabase Storage in private app-files bucket
  const { filePath } = await uploadToStorage({
    file,
    featureName: category,
    itemId,
  });

  // Store metadata in user_files table
  const { data: record, error: dbError } = await supabase
    .from("user_files")
    .insert([
      {
        user_id: user.id,
        file_name: file.name,
        file_path: filePath,
        file_size: file.size,
        file_type: file.type || "application/octet-stream",
        category,
      },
    ])
    .select()
    .single();

  if (dbError) {
    // Roll back uploaded file from storage if DB insertion fails
    await deleteFromStorage(filePath);
    throw dbError;
  }

  // Get signed URL for immediate preview/access
  const signedUrl = await getFileSignedUrl(filePath);

  return {
    ...record,
    signedUrl,
  };
}

/**
 * Loads all files for the authenticated user, optionally filtered by category.
 * Row-Level Security automatically restricts the query to auth.uid().
 */
export async function fetchUserFiles(category = null) {
  let query = supabase
    .from("user_files")
    .select("*")
    .order("created_at", { ascending: false });

  if (category && category !== "all") {
    query = query.eq("category", category);
  }

  const { data, error } = await query;
  if (error) throw error;

  // Generate signed URLs for each private file
  const filesWithSignedUrls = await Promise.all(
    (data || []).map(async (f) => {
      const signedUrl = await getFileSignedUrl(f.file_path);
      return {
        ...f,
        signedUrl,
      };
    })
  );

  return filesWithSignedUrls;
}

/**
 * Deletes a file from both Supabase Storage and the database.
 */
export async function deleteUserFile(fileId, filePath) {
  // 1. Delete from Storage
  if (filePath) {
    await deleteFromStorage(filePath);
  }

  // 2. Delete record from database
  const { error } = await supabase
    .from("user_files")
    .delete()
    .eq("id", fileId);

  if (error) throw error;
  return true;
}

