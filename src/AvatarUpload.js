import React, { useState, useEffect } from "react";
import {
  uploadUserAvatar,
  deleteUserAvatar,
  getFileSignedUrl,
} from "./storageService";
import { supabase } from "./supabaseClient";

export default function AvatarUpload() {
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [avatarPath, setAvatarPath] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Load current avatar on mount
  useEffect(() => {
    loadCurrentAvatar();
  }, []);

  const loadCurrentAvatar = async () => {
    try {
      const { data: { user } } = await supabase.auth.getUser();
      if (!user) return;

      const { data, error } = await supabase
        .from("profiles")
        .select("avatar_path")
        .eq("id", user.id)
        .single();

      if (error && error.code !== "PGRST116") {
        console.error("Error loading profile:", error);
      }

      if (data?.avatar_path) {
        setAvatarPath(data.avatar_path);
        // Bucket is private, generate signed URL
        const signed = await getFileSignedUrl(data.avatar_path);
        setAvatarUrl(signed);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploading(true);
      setErrorMessage("");

      // If an existing avatar exists, clean it up first
      if (avatarPath) {
        await deleteUserAvatar(avatarPath);
      }

      const { filePath, signedUrl } = await uploadUserAvatar(file);
      setAvatarPath(filePath);
      setAvatarUrl(signedUrl);
    } catch (err) {
      setErrorMessage(err.message || "Failed to upload avatar.");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async () => {
    if (!avatarPath) return;

    try {
      setUploading(true);
      setErrorMessage("");
      await deleteUserAvatar(avatarPath);
      setAvatarPath(null);
      setAvatarUrl(null);
    } catch (err) {
      setErrorMessage(err.message || "Failed to delete avatar.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="avatar-upload-component" style={{ marginBottom: "16px" }}>
      {errorMessage && (
        <p style={{ color: "red", fontSize: "0.85rem" }}>{errorMessage}</p>
      )}

      <div style={{ display: "flex", alignItems: "center", gap: "14px" }}>
        {avatarUrl ? (
          <img
            src={avatarUrl}
            alt="User Avatar"
            style={{
              width: "72px",
              height: "72px",
              borderRadius: "50%",
              objectFit: "cover",
              border: "2px solid #ccc",
            }}
          />
        ) : (
          <div
            style={{
              width: "72px",
              height: "72px",
              borderRadius: "50%",
              backgroundColor: "#e0e0e0",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "#757575",
              fontSize: "0.8rem",
            }}
          >
            No Avatar
          </div>
        )}

        <div>
          <label style={{ cursor: "pointer", display: "inline-block", marginRight: "8px" }}>
            <span style={{ padding: "6px 12px", background: "#f0f0f0", border: "1px solid #ccc", borderRadius: "4px" }}>
              {uploading ? "Uploading..." : "Upload Avatar"}
            </span>
            <input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              disabled={uploading}
              style={{ display: "none" }}
            />
          </label>

          {avatarPath && (
            <button
              onClick={handleDelete}
              disabled={uploading}
              style={{ padding: "6px 12px", color: "red", cursor: "pointer", background: "none", border: "1px solid red", borderRadius: "4px" }}
            >
              Remove
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
