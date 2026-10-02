import React, { useState, useEffect, useRef } from "react";
import { supabase } from "./supabaseClient";
import {
  getQuests,
  createQuest,
  updateQuest,
  deleteQuest,
} from "./questsService";
import {
  getSubscriptionStatus,
  subscribeToSubscription,
  FREE_PLAN_QUEST_LIMIT,
  openCustomerPortal,
  toggleSimulatedPro,
} from "./stripeService";
import AvatarUpload from "./AvatarUpload";
import QuestAttachment from "./QuestAttachment";
import FileUploadManager from "./FileUploadManager";
import ProUpgradeModal from "./ProUpgradeModal";
import ProGameRewards from "./ProGameRewards";

export default function Quests() {
  const [quests, setQuests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  // Stripe Pro Subscription state
  const [isPro, setIsPro] = useState(false);
  const [subStatus, setSubStatus] = useState("free");
  const [isUpgradeModalOpen, setIsUpgradeModalOpen] = useState(false);
  const [upgradeModalReason, setUpgradeModalReason] = useState("");

  // New quest form state
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("General");
  const [difficulty, setDifficulty] = useState("NORMAL");
  const [recurrence, setRecurrence] = useState("DAILY");
  const [attachmentFile, setAttachmentFile] = useState(null);
  const questFileInputRef = useRef(null);
  const [submitting, setSubmitting] = useState(false);

  // Edit quest state
  const [editingId, setEditingId] = useState(null);
  const [editTitle, setEditTitle] = useState("");

  // Load user's data & subscription on mount
  useEffect(() => {
    let unsubscribeSub = null;

    const init = async () => {
      await loadSubscription();
      await loadQuests();

      const { data: { user } } = await supabase.auth.getUser();
      if (user) {
        unsubscribeSub = subscribeToSubscription(user.id, () => {
          loadSubscription();
        });
      }
    };

    init();

    return () => {
      if (unsubscribeSub) unsubscribeSub();
    };
  }, []);

  const loadSubscription = async () => {
    try {
      const res = await getSubscriptionStatus();
      setIsPro(res.isPro);
      setSubStatus(res.status);
    } catch (err) {
      console.error("Error checking subscription:", err);
    }
  };

  const loadQuests = async () => {
    try {
      setLoading(true);
      setErrorMessage("");
      const data = await getQuests();
      setQuests(data);
    } catch (err) {
      setErrorMessage(err.message || "Failed to load quests.");
    } finally {
      setLoading(false);
    }
  };

  const openUpgradeModal = (reason) => {
    setUpgradeModalReason(reason || "Upgrade to Pro to create unlimited quests and unlock pets & items!");
    setIsUpgradeModalOpen(true);
  };

  // Create a new quest
  const handleCreate = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;

    // Feature Protection: Free tier allows maximum 3 quests
    if (!isPro && quests.length >= FREE_PLAN_QUEST_LIMIT) {
      openUpgradeModal(
        `Free plan limit reached (${FREE_PLAN_QUEST_LIMIT} quests). Upgrade to Pro to have no limits in making quests or habits!`
      );
      return;
    }

    try {
      setSubmitting(true);
      setErrorMessage("");
      const newQuest = await createQuest({
        title,
        description,
        category,
        difficulty,
        recurrence,
        attachmentFile,
      });
      setQuests((prev) => [newQuest, ...prev]);
      setTitle("");
      setDescription("");
      setAttachmentFile(null);
      if (questFileInputRef.current) questFileInputRef.current.value = "";
    } catch (err) {
      if (err.message?.includes("Free plan limit reached")) {
        openUpgradeModal(
          `Free plan limit reached (${FREE_PLAN_QUEST_LIMIT} quests). Upgrade to Pro to create unlimited quests and habits!`
        );
      } else {
        setErrorMessage(err.message || "Failed to create quest.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  // Update a quest
  const handleUpdate = async (id) => {
    if (!editTitle.trim()) return;

    try {
      setErrorMessage("");
      const updated = await updateQuest(id, { title: editTitle });
      setQuests((prev) => prev.map((q) => (q.id === id ? updated : q)));
      setEditingId(null);
      setEditTitle("");
    } catch (err) {
      setErrorMessage(err.message || "Failed to update quest.");
    }
  };

  // Delete a quest
  const handleDelete = async (id) => {
    try {
      setErrorMessage("");
      await deleteQuest(id);
      setQuests((prev) => prev.filter((q) => q.id !== id));
    } catch (err) {
      setErrorMessage(err.message || "Failed to delete quest.");
    }
  };

  const handleAttachmentUpdated = (questId, newPath) => {
    setQuests((prev) =>
      prev.map((q) => (q.id === questId ? { ...q, attachment_path: newPath } : q))
    );
  };

  const handleProStatusChanged = async (newProStatus) => {
    setIsPro(newProStatus);
    await loadSubscription();
  };

  return (
    <div className="quests-container" style={{ maxWidth: "680px", margin: "0 auto", padding: "20px" }}>
      {/* 1. Stripe Subscription Banner / Header */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          backgroundColor: isPro ? "#f5f3ff" : "#fef3c7",
          border: isPro ? "1px solid #c4b5fd" : "1px solid #fde68a",
          borderRadius: "12px",
          padding: "14px 18px",
          marginBottom: "20px",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <span style={{ fontSize: "1.4rem" }}>{isPro ? "👑" : "🛡️"}</span>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span
                style={{
                  fontWeight: "bold",
                  fontSize: "0.95rem",
                  color: isPro ? "#5b21b6" : "#92400e",
                }}
              >
                {isPro ? "PRO MEMBERSHIP ACTIVE" : "FREE PLAN"}
              </span>
              <span
                style={{
                  fontSize: "0.72rem",
                  backgroundColor: isPro ? "#8b5cf6" : "#d97706",
                  color: "#ffffff",
                  padding: "2px 8px",
                  borderRadius: "9999px",
                  fontWeight: "600",
                }}
              >
                {isPro ? "UNLIMITED ACCESS" : `${quests.length}/${FREE_PLAN_QUEST_LIMIT} Quests Used`}
              </span>
            </div>
            <p style={{ margin: "2px 0 0 0", fontSize: "0.78rem", color: isPro ? "#6d28d9" : "#78350f" }}>
              {isPro
                ? "No limits on quests or habits. Exclusive mythical pets & items unlocked!"
                : "Free plan is limited to 3 quests. Upgrade for unlimited quests, habits, pets & items."}
            </p>
          </div>
        </div>

        {/* Upgrade / Manage Subscription CTA */}
        {isPro ? (
          <div style={{ display: "flex", gap: "8px" }}>
            <button
              onClick={openCustomerPortal}
              style={{
                backgroundColor: "#ffffff",
                border: "1px solid #7c3aed",
                color: "#7c3aed",
                padding: "8px 14px",
                borderRadius: "8px",
                fontWeight: "600",
                fontSize: "0.8rem",
                cursor: "pointer",
              }}
            >
              Manage Billing
            </button>
            <button
              onClick={async () => {
                await toggleSimulatedPro(false);
                setIsPro(false);
              }}
              title="Reset Pro for testing"
              style={{
                backgroundColor: "transparent",
                border: "none",
                color: "#6b7280",
                fontSize: "0.72rem",
                cursor: "pointer",
                textDecoration: "underline",
              }}
            >
              Reset to Free
            </button>
          </div>
        ) : (
          <button
            onClick={() => openUpgradeModal("Upgrade to Pro to unlock unlimited quests and mythical companion pets!")}
            style={{
              backgroundColor: "#d97706",
              color: "#ffffff",
              border: "none",
              padding: "8px 16px",
              borderRadius: "8px",
              fontWeight: "bold",
              fontSize: "0.85rem",
              cursor: "pointer",
              boxShadow: "0 2px 4px rgba(217, 119, 6, 0.3)",
            }}
          >
            ⭐ Upgrade to Pro
          </button>
        )}
      </div>

      {/* User Avatar Section (Supabase Storage: app-files) */}
      <AvatarUpload />

      {/* User File Upload & Management (Progression, Quests, etc.) */}
      <FileUploadManager />

      {/* 2. Unlocked Pets and Mythic Items Showcase */}
      <ProGameRewards isPro={isPro} onOpenUpgradeModal={openUpgradeModal} />

      {/* 3. My Quests Section */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "12px" }}>
        <h2 style={{ margin: 0 }}>My Quests</h2>
        <span style={{ fontSize: "0.85rem", color: "#6b7280" }}>
          {isPro ? "Unlimited Quests (Pro)" : `${quests.length}/${FREE_PLAN_QUEST_LIMIT} Active Quests`}
        </span>
      </div>

      {errorMessage && (
        <div style={{ color: "red", backgroundColor: "#ffebee", padding: "10px", borderRadius: "4px", marginBottom: "16px" }}>
          {errorMessage}
        </div>
      )}

      {/* Create New Quest Form */}
      <form onSubmit={handleCreate} style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "24px" }}>
        <input
          type="text"
          placeholder="New quest title (e.g. Read 20 pages)"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          style={{ padding: "8px", borderRadius: "6px", border: "1px solid #ccc" }}
        />
        <input
          type="text"
          placeholder="Description (optional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ padding: "8px", borderRadius: "6px", border: "1px solid #ccc" }}
        />
        <div style={{ display: "flex", gap: "10px" }}>
          <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)} style={{ padding: "8px", flex: 1, borderRadius: "6px", border: "1px solid #ccc" }}>
            <option value="EASY">Easy (5 EXP)</option>
            <option value="NORMAL">Normal (10 EXP)</option>
            <option value="HARD">Hard (20 EXP)</option>
            <option value="EPIC">Epic (40 EXP)</option>
          </select>
          <select value={recurrence} onChange={(e) => setRecurrence(e.target.value)} style={{ padding: "8px", flex: 1, borderRadius: "6px", border: "1px solid #ccc" }}>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="ONE_TIME">One-Time</option>
          </select>
        </div>

        {/* Feature 1: "Upload File" button when making a quest */}
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <input
            type="file"
            ref={questFileInputRef}
            onChange={(e) => setAttachmentFile(e.target.files?.[0] || null)}
            style={{ display: "none" }}
          />
          <button
            type="button"
            onClick={() => questFileInputRef.current?.click()}
            style={{
              padding: "6px 12px",
              backgroundColor: "#f5f5f5",
              border: "1px solid #ccc",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "0.85rem",
            }}
          >
            {attachmentFile ? `📎 ${attachmentFile.name}` : "📎 Upload File / Proof"}
          </button>
          {attachmentFile && (
            <button
              type="button"
              onClick={() => {
                setAttachmentFile(null);
                if (questFileInputRef.current) questFileInputRef.current.value = "";
              }}
              style={{
                background: "none",
                border: "none",
                color: "red",
                cursor: "pointer",
                fontSize: "0.8rem",
              }}
            >
              ✕ Remove
            </button>
          )}
        </div>

        <button
          type="submit"
          disabled={submitting}
          style={{
            padding: "10px",
            cursor: "pointer",
            backgroundColor: "#4f46e5",
            color: "#ffffff",
            border: "none",
            borderRadius: "6px",
            fontWeight: "bold",
          }}
        >
          {submitting
            ? "Adding Quest..."
            : !isPro && quests.length >= FREE_PLAN_QUEST_LIMIT
            ? "🔒 Upgrade to Pro to Add More Quests"
            : "+ Add Quest"}
        </button>
      </form>

      {/* Quest List */}
      {loading ? (
        <p>Loading quests from Supabase...</p>
      ) : quests.length === 0 ? (
        <p>No quests found. Add your first quest above!</p>
      ) : (
        <ul style={{ listStyle: "none", padding: 0, display: "flex", flexDirection: "column", gap: "12px" }}>
          {quests.map((quest) => (
            <li
              key={quest.id}
              style={{
                border: "1px solid #ddd",
                borderRadius: "8px",
                padding: "14px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                backgroundColor: "#ffffff",
              }}
            >
              {editingId === quest.id ? (
                <div style={{ display: "flex", gap: "8px", flex: 1 }}>
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    style={{ flex: 1, padding: "6px", borderRadius: "4px", border: "1px solid #ccc" }}
                  />
                  <button onClick={() => handleUpdate(quest.id)} style={{ padding: "6px 12px" }}>Save</button>
                  <button onClick={() => setEditingId(null)} style={{ padding: "6px 12px" }}>Cancel</button>
                </div>
              ) : (
                <div style={{ flex: 1 }}>
                  <h4 style={{ margin: "0 0 4px 0" }}>{quest.title}</h4>
                  {quest.description && <p style={{ margin: "0 0 6px 0", color: "#666", fontSize: "0.875rem" }}>{quest.description}</p>}
                  <span style={{ fontSize: "0.75rem", backgroundColor: "#f0f0f0", padding: "2px 8px", borderRadius: "4px", marginRight: "6px" }}>
                    {quest.difficulty}
                  </span>
                  <span style={{ fontSize: "0.75rem", backgroundColor: "#f0f0f0", padding: "2px 8px", borderRadius: "4px" }}>
                    {quest.recurrence}
                  </span>

                  {/* Quest Attachment / File upload component */}
                  <QuestAttachment
                    questId={quest.id}
                    currentAttachmentPath={quest.attachment_path}
                    onAttachmentChange={(newPath) => handleAttachmentUpdated(quest.id, newPath)}
                  />
                </div>
              )}

              <div style={{ display: "flex", gap: "8px", marginLeft: "12px" }}>
                {editingId !== quest.id && (
                  <button
                    onClick={() => {
                      setEditingId(quest.id);
                      setEditTitle(quest.title);
                    }}
                    style={{ padding: "6px 10px", cursor: "pointer" }}
                  >
                    Edit
                  </button>
                )}
                <button
                  onClick={() => handleDelete(quest.id)}
                  style={{ padding: "6px 10px", cursor: "pointer", color: "red" }}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {/* Pro Upgrade Modal */}
      <ProUpgradeModal
        isOpen={isUpgradeModalOpen}
        onClose={() => setIsUpgradeModalOpen(false)}
        onProStatusChanged={handleProStatusChanged}
        triggerReason={upgradeModalReason}
      />
    </div>
  );
}
