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

    // Client-side SaaS limit check (Notes count >= 3)
    if (totalNotes >= FREE_PLAN_NOTE_LIMIT) {
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
        {/* TOTAL NOTES CARD (Live Supabase Count) */}
        <div
          className="metric-card total-notes-card"
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
          }}
        >
          <div style={{ color: "#718096", fontSize: "0.875rem", fontWeight: "600", marginBottom: "8px" }}>
            Total Notes
          </div>
          <div style={{ fontSize: "2rem", fontWeight: "800", color: "#2b6cb0" }}>
            {loading ? "..." : totalNotes}
          </div>
          <p style={{ margin: "6px 0 0 0", fontSize: "0.75rem", color: "#a0aec0" }}>
            Real-time count for authenticated user
          </p>
        </div>

        {/* PLAN LIMIT CARD */}
        <div
          className="metric-card"
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
          }}
        >
          <div style={{ color: "#718096", fontSize: "0.875rem", fontWeight: "600", marginBottom: "8px" }}>
            Plan Limit
          </div>
          <div style={{ fontSize: "2rem", fontWeight: "800", color: "#2d3748" }}>
            {FREE_PLAN_NOTE_LIMIT}
          </div>
          <p style={{ margin: "6px 0 0 0", fontSize: "0.75rem", color: "#a0aec0" }}>
            Maximum free notes allowed
          </p>
        </div>

        {/* PLAN USAGE CARD */}
        <div
          className="metric-card"
          style={{
            backgroundColor: "#fff",
            border: "1px solid #e2e8f0",
            borderRadius: "8px",
            padding: "20px",
            boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
          }}
        >
          <div style={{ color: "#718096", fontSize: "0.875rem", fontWeight: "600", marginBottom: "8px" }}>
            Quota Status
          </div>
          <div
            style={{
              fontSize: "1.25rem",
              fontWeight: "700",
              color: totalNotes >= FREE_PLAN_NOTE_LIMIT ? "#e53e3e" : "#38a169",
              marginTop: "4px",
            }}
          >
            {totalNotes >= FREE_PLAN_NOTE_LIMIT ? "Limit Reached" : `${FREE_PLAN_NOTE_LIMIT - totalNotes} Available`}
          </div>
          <p style={{ margin: "10px 0 0 0", fontSize: "0.75rem", color: "#a0aec0" }}>
            {totalNotes >= FREE_PLAN_NOTE_LIMIT ? "Upgrade for unlimited access" : "Free tier quota"}
          </p>
        </div>
      </div>

      {/* Error alert */}
      {errorMessage && (
        <div
          style={{
            backgroundColor: "#fff5f5",
            color: "#c53030",
            padding: "12px",
            borderRadius: "6px",
            marginBottom: "16px",
            fontSize: "0.875rem",
            border: "1px solid #feb2b2",
          }}
        >
          {errorMessage}
        </div>
      )}

      {/* Create Note Section */}
      <section
        style={{
          backgroundColor: "#fff",
          border: "1px solid #e2e8f0",
          borderRadius: "8px",
          padding: "20px",
          marginBottom: "28px",
        }}
      >
        <h3 style={{ fontSize: "1.1rem", margin: "0 0 16px 0", color: "#2d3748" }}>
          Create New Note
        </h3>

        <form onSubmit={handleCreateNote} style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
          <input
            type="text"
            placeholder="Note Title (e.g. AI Meeting Summary)"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            required
            style={{
              padding: "10px 12px",
              border: "1px solid #cbd5e0",
              borderRadius: "6px",
              fontSize: "0.9rem",
            }}
          />
          <textarea
            placeholder="Note content..."
            value={newContent}
            onChange={(e) => setNewContent(e.target.value)}
            rows={3}
            style={{
              padding: "10px 12px",
              border: "1px solid #cbd5e0",
              borderRadius: "6px",
              fontSize: "0.9rem",
              fontFamily: "inherit",
              resize: "vertical",
            }}
          />
          <button
            type="submit"
            disabled={creating}
            style={{
              alignSelf: "flex-start",
              backgroundColor: totalNotes >= FREE_PLAN_NOTE_LIMIT ? "#a0aec0" : "#3182ce",
              color: "#fff",
              border: "none",
              borderRadius: "6px",
              padding: "10px 18px",
              fontWeight: "600",
              fontSize: "0.875rem",
              cursor: creating ? "not-allowed" : "pointer",
            }}
          >
            {creating ? "Saving..." : "+ Create Note"}
          </button>
        </form>
      </section>

      {/* Notes List */}
      <section>
        <h3 style={{ fontSize: "1.1rem", margin: "0 0 14px 0", color: "#2d3748" }}>
          Your Notes ({totalNotes})
        </h3>

        {loading ? (
          <p style={{ color: "#718096", fontSize: "0.9rem" }}>Loading notes...</p>
        ) : notes.length === 0 ? (
          <p style={{ color: "#a0aec0", fontStyle: "italic", fontSize: "0.9rem" }}>
            No notes created yet. Use the form above to create your first note.
          </p>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            {notes.map((note) => (
              <div
                key={note.id}
                style={{
                  backgroundColor: "#fff",
                  border: "1px solid #e2e8f0",
                  borderRadius: "8px",
                  padding: "16px",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "flex-start",
                }}
              >
                <div style={{ flex: 1, marginRight: "16px" }}>
                  <h4 style={{ margin: "0 0 6px 0", fontSize: "1rem", color: "#1a202c" }}>
                    {note.title}
                  </h4>
                  {note.content && (
                    <p style={{ margin: 0, color: "#4a5568", fontSize: "0.875rem", whiteSpace: "pre-wrap" }}>
                      {note.content}
                    </p>
                  )}
                  <span style={{ fontSize: "0.75rem", color: "#a0aec0", display: "inline-block", marginTop: "8px" }}>
                    Created: {new Date(note.created_at).toLocaleDateString()}
                  </span>
                </div>
                <button
                  type="button"
                  onClick={() => handleDeleteNote(note.id)}
                  style={{
                    backgroundColor: "transparent",
                    color: "#e53e3e",
                    border: "1px solid #fed7d7",
                    borderRadius: "4px",
                    padding: "6px 12px",
                    fontSize: "0.8rem",
                    cursor: "pointer",
                  }}
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Clean SaaS Plan Limit Modal */}
      {showLimitModal && (
        <div
          role="dialog"
          aria-modal="true"
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0, 0, 0, 0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 1000,
            padding: "20px",
          }}
        >
          <div
            style={{
              backgroundColor: "#fff",
              borderRadius: "12px",
              padding: "28px",
              maxWidth: "420px",
              width: "100%",
              boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1)",
              textAlign: "center",
            }}
          >
            <div
              style={{
                width: "48px",
                height: "48px",
                backgroundColor: "#feebc8",
                color: "#c05621",
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                margin: "0 auto 16px auto",
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
