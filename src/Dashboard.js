import React, { useState, useEffect } from "react";
import { supabase } from "./supabaseClient";
import {
  getNotesCount,
  getNotes,
  createNote,
  deleteNote,
  subscribeToNotes,
  FREE_PLAN_NOTE_LIMIT,
} from "./notesService";
import {
  getSubscriptionStatus,
  subscribeToSubscription,
  openCustomerPortal,
} from "./stripeService";
import ProUpgradeModal from "./ProUpgradeModal";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [totalNotes, setTotalNotes] = useState(0);
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isPro, setIsPro] = useState(false);

  // Form state
  const [newTitle, setNewTitle] = useState("");
  const [newContent, setNewContent] = useState("");
  const [creating, setCreating] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  // SaaS Plan Limit Modal state
  const [showLimitModal, setShowLimitModal] = useState(false);
  const [showProModal, setShowProModal] = useState(false);

  useEffect(() => {
    let unsubscribe = null;
    let unsubscribeSub = null;

    const initDashboard = async () => {
      try {
        setLoading(true);
        // 1. Fetch current authenticated user
        const { data: { user: currentUser } } = await supabase.auth.getUser();
        setUser(currentUser);

        if (currentUser) {
          // 2. Count notes and load list
          await refreshData(currentUser.id);

          // 3. Check Pro subscription status
          const sub = await getSubscriptionStatus();
          setIsPro(sub.isPro);

          // 4. Set up real-time subscription for automatic updates
          unsubscribe = subscribeToNotes(currentUser.id, () => {
            refreshData(currentUser.id);
          });

          unsubscribeSub = subscribeToSubscription(currentUser.id, async () => {
            const updatedSub = await getSubscriptionStatus();
            setIsPro(updatedSub.isPro);
          });
        }
      } catch (err) {
        setErrorMessage(err.message || "Failed to load dashboard data.");
      } finally {
        setLoading(false);
      }
    };

    initDashboard();

    return () => {
      if (unsubscribe) unsubscribe();
      if (unsubscribeSub) unsubscribeSub();
    };
  }, []);

  const refreshData = async (userId) => {
    try {
      const [count, notesList] = await Promise.all([
        getNotesCount(),
        getNotes(),
      ]);
      setTotalNotes(count);
      setNotes(notesList);
    } catch (err) {
      console.error("Error refreshing notes count:", err);
    }
  };

  const handleCreateNote = async (e) => {
    e.preventDefault();
    if (!newTitle.trim()) return;

    // Client-side SaaS limit check (Notes count >= 3 on free plan)
    if (!isPro && totalNotes >= FREE_PLAN_NOTE_LIMIT) {
      setShowLimitModal(true);
      return;
    }

    try {
      setCreating(true);
      setErrorMessage("");
      await createNote({ title: newTitle, content: newContent });
      setNewTitle("");
      setNewContent("");

      // Immediately refresh real count and list
      if (user) {
        await refreshData(user.id);
      }
    } catch (err) {
      if (err.message?.includes("Free plan limit reached")) {
        setShowLimitModal(true);
      } else {
        setErrorMessage(err.message || "Failed to create note.");
      }
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteNote = async (noteId) => {
    try {
      setErrorMessage("");
      await deleteNote(noteId);

      // Automatically refresh real count and list
      if (user) {
        await refreshData(user.id);
      }
    } catch (err) {
      setErrorMessage(err.message || "Failed to delete note.");
    }
  };

  return (
    <div style={{ maxWidth: "880px", margin: "0 auto", padding: "24px", fontFamily: "sans-serif" }}>
      {/* SaaS Header */}
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          borderBottom: "1px solid #e2e8f0",
          paddingBottom: "16px",
          marginBottom: "24px",
        }}
      >
        <div>
          <h1 style={{ fontSize: "1.5rem", fontWeight: "700", margin: 0, color: "#1a202c" }}>
            AI Notes Dashboard
          </h1>
          <p style={{ margin: "4px 0 0 0", color: "#718096", fontSize: "0.875rem" }}>
            {user ? user.email : "Signed In"}
          </p>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          {isPro ? (
            <>
              <span
                style={{
                  backgroundColor: "#ede9fe",
                  color: "#6d28d9",
                  padding: "4px 10px",
                  borderRadius: "16px",
                  fontSize: "0.75rem",
                  fontWeight: "bold",
                }}
              >
                ⭐ PRO Member (Unlimited Notes & Quests)
              </span>
              <button
                onClick={openCustomerPortal}
                style={{
                  backgroundColor: "#ffffff",
                  color: "#6d28d9",
                  border: "1px solid #7c3aed",
                  borderRadius: "4px",
                  padding: "6px 12px",
                  fontSize: "0.8rem",
                  fontWeight: "600",
                  cursor: "pointer",
                }}
              >
                Manage Billing
              </button>
            </>
          ) : (
            <>
              <span
                style={{
                  backgroundColor: "#edf2f7",
                  color: "#4a5568",
                  padding: "4px 10px",
                  borderRadius: "16px",
                  fontSize: "0.75rem",
                  fontWeight: "600",
                }}
              >
                Free Plan ({totalNotes}/{FREE_PLAN_NOTE_LIMIT} notes)
              </span>
              <button
                onClick={() => setShowProModal(true)}
                style={{
                  backgroundColor: "#3182ce",
                  color: "#fff",
                  border: "none",
                  borderRadius: "4px",
                  padding: "6px 12px",
                  fontSize: "0.8rem",
                  fontWeight: "600",
                  cursor: "pointer",
                }}
              >
                Upgrade to Pro
              </button>
            </>
          )}
        </div>
      </header>

      {/* SaaS Metric Cards */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: "16px",
          marginBottom: "28px",
        }}
      >
        {/* Total Notes Card - Connected to real authenticated count */}
        <div
          className="metric-card-total-notes"
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px 0 rgba(0, 0, 0, 0.05)",
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span style={{ fontSize: "0.875rem", fontWeight: "600", color: "#718096" }}>
              Total Notes
            </span>
            <span
              style={{
                fontSize: "0.75rem",
                backgroundColor: "#ebf8ff",
                color: "#2b6cb0",
                padding: "2px 8px",
                borderRadius: "12px",
                fontWeight: "500",
              }}
            >
              Real-time
            </span>
          </div>

          <div style={{ marginTop: "12px" }}>
            {loading ? (
              <span style={{ fontSize: "1.5rem", color: "#a0aec0", fontWeight: "600" }}>...</span>
            ) : (
              <div style={{ display: "flex", alignItems: "baseline", gap: "8px" }}>
                <span
                  style={{
                    fontSize: "2.25rem",
                    fontWeight: "800",
                    color: "#2d3748",
                    lineHeight: 1,
                  }}
                >
                  {totalNotes}
                </span>
                <span style={{ fontSize: "0.875rem", color: "#718096" }}>
                  {isPro ? "unlimited" : `/ ${FREE_PLAN_NOTE_LIMIT} free limit`}
                </span>
              </div>
            )}
          </div>

          <p style={{ margin: "8px 0 0 0", fontSize: "0.75rem", color: "#a0aec0" }}>
            Live count for authenticated user
          </p>
        </div>

        {/* Secondary SaaS Card: Plan Status */}
        <div
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px 0 rgba(0, 0, 0, 0.05)",
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <span style={{ fontSize: "0.875rem", fontWeight: "600", color: "#718096" }}>
              Current Plan
            </span>
            <span
              style={{
                fontSize: "0.75rem",
                backgroundColor: isPro ? "#f3e8ff" : "#f7fafc",
                color: isPro ? "#7e22ce" : "#4a5568",
                padding: "2px 8px",
                borderRadius: "12px",
                fontWeight: "bold",
              }}
            >
              {isPro ? "PRO TIER" : "FREE TIER"}
            </span>
          </div>

          <div style={{ marginTop: "12px" }}>
            <div style={{ fontSize: "1.75rem", fontWeight: "700", color: isPro ? "#6d28d9" : "#2d3748" }}>
              {isPro ? "Pro Active ⭐" : "Free Explorer"}
            </div>
            <p style={{ margin: "6px 0 0 0", fontSize: "0.75rem", color: "#718096" }}>
              {isPro ? "Unlimited quests, habits & mythic companion pets" : "Upgrade to unlock unlimited quests, pets & items"}
            </p>
          </div>
        </div>
      </div>

      {/* Error Notice */}
      {errorMessage && (
        <div
          style={{
            backgroundColor: "#fff5f5",
            border: "1px solid #feb2b2",
            color: "#c53030",
            padding: "12px 16px",
            borderRadius: "6px",
            marginBottom: "20px",
            fontSize: "0.875rem",
          }}
        >
          {errorMessage}
        </div>
      )}

      {/* Main SaaS Layout: Create Note + Notes List */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: "24px" }}>
        {/* Create Note Section */}
        <section
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px 0 rgba(0, 0, 0, 0.05)",
          }}
        >
          <h2 style={{ fontSize: "1.125rem", fontWeight: "600", margin: "0 0 16px 0", color: "#2d3748" }}>
            Create New Note
          </h2>

          <form onSubmit={handleCreateNote}>
            <div style={{ marginBottom: "12px" }}>
              <input
                type="text"
                placeholder="Note Title..."
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                required
                style={{
                  width: "100%",
                  padding: "10px 12px",
                  borderRadius: "6px",
                  border: "1px solid #cbd5e0",
                  fontSize: "0.875rem",
                  boxSizing: "border-box",
                }}
              />
            </div>

            <div style={{ marginBottom: "16px" }}>
              <textarea
                placeholder="Write your note content here..."
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                rows={3}
                style={{
                  width: "100%",
                  padding: "10px 12px",
                  borderRadius: "6px",
                  border: "1px solid #cbd5e0",
                  fontSize: "0.875rem",
                  boxSizing: "border-box",
                  resize: "vertical",
                }}
              />
            </div>

            <button
              type="submit"
              disabled={creating}
              style={{
                backgroundColor: !isPro && totalNotes >= FREE_PLAN_NOTE_LIMIT ? "#a0aec0" : "#3182ce",
                color: "#fff",
                border: "none",
                borderRadius: "6px",
                padding: "10px 20px",
                fontSize: "0.875rem",
                fontWeight: "600",
                cursor: creating ? "not-allowed" : "pointer",
                transition: "background 0.2s",
              }}
            >
              {creating ? "Saving..." : !isPro && totalNotes >= FREE_PLAN_NOTE_LIMIT ? "Limit Reached (Upgrade)" : "+ Create Note"}
            </button>
          </form>
        </section>

        {/* Existing Notes Feed */}
        <section
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px 0 rgba(0, 0, 0, 0.05)",
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
            <h2 style={{ fontSize: "1.125rem", fontWeight: "600", margin: 0, color: "#2d3748" }}>
              My Notes ({notes.length})
            </h2>
          </div>

          {loading ? (
            <p style={{ color: "#a0aec0", fontSize: "0.875rem" }}>Loading notes...</p>
          ) : notes.length === 0 ? (
            <p style={{ color: "#a0aec0", fontSize: "0.875rem" }}>
              No notes yet. Create your first note above!
            </p>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
              {notes.map((note) => (
                <div
                  key={note.id}
                  style={{
                    border: "1px solid #edf2f7",
                    borderRadius: "6px",
                    padding: "16px",
                    backgroundColor: "#f7fafc",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-start",
                  }}
                >
                  <div style={{ flex: 1, marginRight: "16px" }}>
                    <h3 style={{ margin: "0 0 4px 0", fontSize: "1rem", fontWeight: "600", color: "#2d3748" }}>
                      {note.title}
                    </h3>
                    {note.content && (
                      <p style={{ margin: "0 0 8px 0", fontSize: "0.875rem", color: "#4a5568", whiteSpace: "pre-wrap" }}>
                        {note.content}
                      </p>
                    )}
                    <span style={{ fontSize: "0.75rem", color: "#a0aec0" }}>
                      {new Date(note.created_at).toLocaleDateString()}
                    </span>
                  </div>

                  <button
                    onClick={() => handleDeleteNote(note.id)}
                    style={{
                      backgroundColor: "transparent",
                      color: "#e53e3e",
                      border: "none",
                      fontSize: "0.8rem",
                      cursor: "pointer",
                      padding: "4px 8px",
                      borderRadius: "4px",
                    }}
                    title="Delete Note"
                  >
                    Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* Free Plan Limit Modal */}
      {showLimitModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0, 0, 0, 0.5)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              borderRadius: "8px",
              padding: "24px",
              maxWidth: "420px",
              width: "90%",
              boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1)",
              textAlign: "center",
            }}
          >
            <div
              style={{
                width: "48px",
                height: "48px",
                borderRadius: "50%",
                backgroundColor: "#feebc8",
                color: "#dd6b20",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                margin: "0 auto 16px",
                fontSize: "1.5rem",
              }}
            >
              ⚠️
            </div>

            <h3 style={{ margin: "0 0 8px 0", fontSize: "1.25rem", color: "#1a202c" }}>
              Plan Limit Reached
            </h3>

            <p style={{ margin: "0 0 20px 0", color: "#4a5568", fontSize: "0.95rem", lineHeight: "1.5" }}>
              Free plan limit reached. Upgrade to Pro to create unlimited notes.
            </p>

            <div style={{ display: "flex", gap: "10px", justifyContent: "center" }}>
              <button
                type="button"
                onClick={() => setShowLimitModal(false)}
                style={{
                  padding: "9px 16px",
                  borderRadius: "6px",
                  border: "1px solid #cbd5e0",
                  backgroundColor: "#edf2f7",
                  color: "#4a5568",
                  cursor: "pointer",
                  fontSize: "0.875rem",
                  fontWeight: "600",
                }}
              >
                Close
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowLimitModal(false);
                  setShowProModal(true);
                }}
                style={{
                  padding: "9px 18px",
                  borderRadius: "6px",
                  border: "none",
                  backgroundColor: "#3182ce",
                  color: "#fff",
                  cursor: "pointer",
                  fontSize: "0.875rem",
                  fontWeight: "600",
                }}
              >
                Upgrade to Pro
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Stripe Pro Upgrade Modal */}
      <ProUpgradeModal
        isOpen={showProModal}
        onClose={() => setShowProModal(false)}
        onProStatusChanged={(newPro) => setIsPro(newPro)}
        triggerReason="Upgrade to Pro to create unlimited notes, unlimited quests, and unlock mythical pets & items."
      />
    </div>
  );
}
