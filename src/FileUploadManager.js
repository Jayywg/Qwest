import React, { useState, useEffect, useRef } from "react";
import {
  uploadUserFile,
  fetchUserFiles,
  deleteUserFile,
} from "./storageService";

export default function FileUploadManager() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [uploadCategory, setUploadCategory] = useState("progression");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const fileInputRef = useRef(null);

  useEffect(() => {
    loadFiles();
  }, [selectedCategory]);

  const loadFiles = async () => {
    try {
      setLoading(true);
      setErrorMessage("");
      const userFiles = await fetchUserFiles(selectedCategory);
      setFiles(userFiles);
    } catch (err) {
      setErrorMessage(err.message || "Failed to load files.");
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploading(true);
      setErrorMessage("");
      setSuccessMessage("");

      // Uploads to app-files under: ${auth.uid()}/${uploadCategory}/general/${uuid}.${extension}
      const uploadedFile = await uploadUserFile({
        file,
        category: uploadCategory,
        itemId: "general",
      });

      setFiles((prev) => [uploadedFile, ...prev]);
      setSuccessMessage(`"${file.name}" uploaded successfully!`);

      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    } catch (err) {
      setErrorMessage(err.message || "Upload failed.");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (fileId, filePath, fileName) => {
    if (!window.confirm(`Delete "${fileName}"? This will remove it from storage.`)) {
      return;
    }

    try {
      setErrorMessage("");
      await deleteUserFile(fileId, filePath);
      setFiles((prev) => prev.filter((f) => f.id !== fileId));
      setSuccessMessage(`"${fileName}" deleted.`);
    } catch (err) {
      setErrorMessage(err.message || "Failed to delete file.");
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
  };

  return (
    <div
      className="file-upload-manager"
      style={{
        border: "1px solid #e0e0e0",
        borderRadius: "8px",
        padding: "16px",
        marginBottom: "24px",
        backgroundColor: "#fafafa",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "12px", flexWrap: "wrap", gap: "8px" }}>
        <h3 style={{ margin: 0 }}>My Files (Private Storage)</h3>

        {/* Upload Controls */}
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <select
            value={uploadCategory}
            onChange={(e) => setUploadCategory(e.target.value)}
            disabled={uploading}
            style={{ padding: "6px 8px", borderRadius: "4px", border: "1px solid #ccc", fontSize: "0.85rem" }}
          >
            <option value="progression">Progression</option>
            <option value="quests">Quests</option>
            <option value="general">General</option>
          </select>

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileSelect}
            disabled={uploading}
            style={{ display: "none" }}
          />

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            style={{
              padding: "7px 14px",
              backgroundColor: "#2e7d32",
              color: "#fff",
              border: "none",
              borderRadius: "4px",
              cursor: uploading ? "not-allowed" : "pointer",
              fontWeight: "bold",
              fontSize: "0.85rem",
            }}
          >
            {uploading ? "Uploading..." : "📁 Upload File"}
          </button>
        </div>
      </div>

      <p style={{ margin: "0 0 12px 0", fontSize: "0.8rem", color: "#666" }}>
        Protected by RLS: You only see files stored under your own user ID.
      </p>

      {/* Filter by Category */}
      <div style={{ display: "flex", gap: "8px", marginBottom: "14px" }}>
        {["all", "progression", "quests", "general"].map((cat) => (
          <button
            key={cat}
            type="button"
            onClick={() => setSelectedCategory(cat)}
            style={{
              padding: "4px 10px",
              fontSize: "0.75rem",
              borderRadius: "16px",
              border: selectedCategory === cat ? "1px solid #2e7d32" : "1px solid #ddd",
              backgroundColor: selectedCategory === cat ? "#e8f5e9" : "#fff",
              color: selectedCategory === cat ? "#2e7d32" : "#555",
              cursor: "pointer",
              textTransform: "capitalize",
            }}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Alerts */}
      {errorMessage && (
        <div style={{ color: "#d32f2f", backgroundColor: "#ffebee", padding: "8px", borderRadius: "4px", marginBottom: "12px", fontSize: "0.85rem" }}>
          {errorMessage}
        </div>
      )}
      {successMessage && (
        <div style={{ color: "#2e7d32", backgroundColor: "#e8f5e9", padding: "8px", borderRadius: "4px", marginBottom: "12px", fontSize: "0.85rem" }}>
          {successMessage}
        </div>
      )}

      {/* File List */}
      {loading ? (
        <p style={{ fontSize: "0.85rem", color: "#888" }}>Loading your files...</p>
      ) : files.length === 0 ? (
        <p style={{ fontSize: "0.85rem", color: "#888", fontStyle: "italic" }}>
          No files uploaded yet in this category. Click "Upload File" above to add one.
        </p>
      ) : (
        <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "flex", flexDirection: "column", gap: "8px" }}>
          {files.map((item) => (
            <li
              key={item.id}
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                backgroundColor: "#fff",
                padding: "10px 12px",
                borderRadius: "6px",
                border: "1px solid #e0e0e0",
                fontSize: "0.85rem",
              }}
            >
              <div style={{ flex: 1, minWidth: 0, marginRight: "12px" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
                  <strong style={{ whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", maxWidth: "220px" }}>
                    {item.file_name}
                  </strong>
                  <span
                    style={{
                      fontSize: "0.7rem",
                      backgroundColor: "#f5f5f5",
                      color: "#616161",
                      padding: "2px 6px",
                      borderRadius: "4px",
                      textTransform: "uppercase",
                    }}
                  >
                    {item.category}
                  </span>
                  <span style={{ fontSize: "0.75rem", color: "#999" }}>
                    {formatFileSize(item.file_size)}
                  </span>
                </div>
              </div>

              <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
                {item.signedUrl && (
                  <a
                    href={item.signedUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{
                      padding: "4px 8px",
                      fontSize: "0.75rem",
                      color: "#1976d2",
                      textDecoration: "none",
                      border: "1px solid #bbdefb",
                      borderRadius: "4px",
                    }}
                  >
                    View / Download
                  </a>
                )}
                <button
                  type="button"
                  onClick={() => handleDelete(item.id, item.file_path, item.file_name)}
                  style={{
                    padding: "4px 8px",
                    fontSize: "0.75rem",
                    color: "#d32f2f",
                    backgroundColor: "transparent",
                    border: "1px solid #ffcdd2",
                    borderRadius: "4px",
                    cursor: "pointer",
                  }}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
