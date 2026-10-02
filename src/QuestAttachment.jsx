import React, { useState, useEffect } from "react";
import {
  uploadQuestAttachment,
  deleteQuestAttachment,
  getFileSignedUrl,
} from "./storageService";

export default function QuestAttachment({ questId, currentAttachmentPath, onAttachmentChange }) {
  const [signedUrl, setSignedUrl] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // Load signed URL when attachment path changes
  useEffect(() => {
    let isMounted = true;
    if (currentAttachmentPath) {
      getFileSignedUrl(currentAttachmentPath).then((url) => {
        if (isMounted) setSignedUrl(url);
      });
    } else {
      setSignedUrl(null);
    }
    return () => {
      isMounted = false;
    };
  }, [currentAttachmentPath]);

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploading(true);
      setErrorMessage("");

      // Remove existing attachment from storage if present
      if (currentAttachmentPath) {
        await deleteQuestAttachment(questId, currentAttachmentPath);
      }

      const { filePath, signedUrl: newSignedUrl } = await uploadQuestAttachment(questId, file);
      setSignedUrl(newSignedUrl);
      if (onAttachmentChange) {
        onAttachmentChange(filePath);
      }
    } catch (err) {
      setErrorMessage(err.message || "Failed to upload attachment.");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async () => {
    if (!currentAttachmentPath) return;

    try {
      setUploading(true);
      setErrorMessage("");
      await deleteQuestAttachment(questId, currentAttachmentPath);
      setSignedUrl(null);
      if (onAttachmentChange) {
        onAttachmentChange(null);
      }
    } catch (err) {
      setErrorMessage(err.message || "Failed to remove attachment.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="quest-attachment-wrapper" style={{ marginTop: "8px" }}>
      {errorMessage && (
        <span style={{ color: "red", fontSize: "0.75rem", display: "block" }}>{errorMessage}</span>
      )}

      {signedUrl ? (
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <a href={signedUrl} target="_blank" rel="noreferrer">
            <img
              src={signedUrl}
              alt="Attachment"
              style={{
                width: "48px",
                height: "48px",
                objectFit: "cover",
                borderRadius: "4px",
                border: "1px solid #ccc",
              }}
            />
          </a>
          <button
            onClick={handleDelete}
            disabled={uploading}
            style={{ fontSize: "0.75rem", color: "red", background: "none", border: "none", cursor: "pointer" }}
          >
            {uploading ? "Removing..." : "Remove file"}
          </button>
        </div>
      ) : (
        <label style={{ cursor: "pointer", fontSize: "0.75rem", color: "#1976d2" }}>
          <span>{uploading ? "Uploading..." : "+ Attach file / proof"}</span>
          <input
            type="file"
            onChange={handleFileChange}
            disabled={uploading}
            style={{ display: "none" }}
          />
        </label>
      )}
    </div>
  );
}
