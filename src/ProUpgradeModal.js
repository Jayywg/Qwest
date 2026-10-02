import React, { useState } from "react";
import {
  STRIPE_PRICES,
  PRO_FEATURES,
  createCheckoutSession,
  toggleSimulatedPro,
} from "./stripeService";

export default function ProUpgradeModal({ isOpen, onClose, onProStatusChanged, triggerReason }) {
  const [selectedInterval, setSelectedInterval] = useState("monthly");
  const [loading, setLoading] = useState(false);
  const [sandboxActive, setSandboxActive] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  if (!isOpen) return null;

  const handleCheckout = async () => {
    try {
      setLoading(true);
      setErrorMsg("");
      const res = await createCheckoutSession(selectedInterval);

      if (res?.sandboxMode) {
        setSandboxActive(true);
      }
    } catch (err) {
      setErrorMsg(err.message || "Failed to initiate Stripe checkout.");
    } finally {
      setLoading(false);
    }
  };

  const handleSandboxActivate = async () => {
    try {
      setLoading(true);
      await toggleSimulatedPro(true);
      if (typeof onProStatusChanged === "function") {
        onProStatusChanged(true);
      }
      onClose();
    } catch (err) {
      setErrorMsg("Failed to simulate Pro: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0, 0, 0, 0.75)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 9999,
        padding: "16px",
      }}
    >
      <div
        style={{
          backgroundColor: "#ffffff",
          borderRadius: "16px",
          maxWidth: "520px",
          width: "100%",
          padding: "24px",
          boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.2)",
          position: "relative",
          maxHeight: "90vh",
          overflowY: "auto",
        }}
      >
        <button
          onClick={onClose}
          style={{
            position: "absolute",
            top: "16px",
            right: "16px",
            background: "none",
            border: "none",
            fontSize: "1.25rem",
            cursor: "pointer",
            color: "#6b7280",
          }}
        >
          ✕
        </button>

        <div style={{ textAlign: "center", marginBottom: "20px" }}>
          <div
            style={{
              display: "inline-block",
              padding: "4px 12px",
              backgroundColor: "#fef3c7",
              color: "#b45309",
              borderRadius: "9999px",
              fontSize: "0.8rem",
              fontWeight: "bold",
              marginBottom: "8px",
            }}
          >
            ⭐ QWEST PRO MEMBERSHIP
          </div>
          <h2 style={{ margin: "4px 0 8px", fontSize: "1.5rem", color: "#111827" }}>
            Unlock the Full Adventurer Experience
          </h2>
          <p style={{ margin: 0, color: "#4b5563", fontSize: "0.9rem" }}>
            {triggerReason || "Unlimited habits & quests, plus mythical companion pets and legendary items."}
          </p>
        </div>

        <div
          style={{
            backgroundColor: "#f9fafb",
            borderRadius: "12px",
            padding: "16px",
            marginBottom: "20px",
            border: "1px solid #e5e7eb",
          }}
        >
          <div style={{ fontSize: "0.85rem", fontWeight: "bold", color: "#374151", marginBottom: "8px" }}>
            WHAT PRO UNLOCKS:
          </div>
          <ul style={{ margin: 0, paddingLeft: "18px", color: "#4b5563", fontSize: "0.875rem", lineHeight: "1.6" }}>
            {PRO_FEATURES.map((feature, idx) => (
              <li key={idx} style={{ marginBottom: "4px" }}>
                <strong>{feature.split(":")[0]}:</strong> {feature.split(":")[1] || ""}
              </li>
            ))}
          </ul>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px", marginBottom: "20px" }}>
          <div
            onClick={() => setSelectedInterval("monthly")}
            style={{
              border: selectedInterval === "monthly" ? "2px solid #6366f1" : "1px solid #d1d5db",
              borderRadius: "10px",
              padding: "14px",
              cursor: "pointer",
              backgroundColor: selectedInterval === "monthly" ? "#eef2ff" : "#ffffff",
              textAlign: "center",
              transition: "all 0.2s",
            }}
          >
            <div style={{ fontSize: "0.85rem", fontWeight: "600", color: "#374151" }}>Monthly</div>
            <div style={{ fontSize: "1.3rem", fontWeight: "bold", color: "#111827", margin: "4px 0" }}>
              {STRIPE_PRICES.MONTHLY.price}
            </div>
            <div style={{ fontSize: "0.75rem", color: "#6b7280" }}>{STRIPE_PRICES.MONTHLY.description}</div>
          </div>

          <div
            onClick={() => setSelectedInterval("yearly")}
            style={{
              border: selectedInterval === "yearly" ? "2px solid #6366f1" : "1px solid #d1d5db",
              borderRadius: "10px",
              padding: "14px",
              cursor: "pointer",
              backgroundColor: selectedInterval === "yearly" ? "#eef2ff" : "#ffffff",
              textAlign: "center",
              position: "relative",
              transition: "all 0.2s",
            }}
          >
            <span
              style={{
                position: "absolute",
                top: "-10px",
                right: "10px",
                backgroundColor: "#10b981",
                color: "#ffffff",
                fontSize: "0.65rem",
                fontWeight: "bold",
                padding: "2px 8px",
                borderRadius: "9999px",
              }}
            >
              {STRIPE_PRICES.YEARLY.badge}
            </span>
            <div style={{ fontSize: "0.85rem", fontWeight: "600", color: "#374151" }}>Yearly</div>
            <div style={{ fontSize: "1.3rem", fontWeight: "bold", color: "#111827", margin: "4px 0" }}>
              {STRIPE_PRICES.YEARLY.price}
            </div>
            <div style={{ fontSize: "0.75rem", color: "#6b7280" }}>{STRIPE_PRICES.YEARLY.description}</div>
          </div>
        </div>

        {errorMsg && (
          <div
            style={{
              backgroundColor: "#fee2e2",
              color: "#991b1b",
              padding: "10px",
              borderRadius: "8px",
              fontSize: "0.85rem",
              marginBottom: "16px",
            }}
          >
            {errorMsg}
          </div>
        )}

        <button
          onClick={handleCheckout}
          disabled={loading}
          style={{
            width: "100%",
            padding: "12px",
            backgroundColor: "#6366f1",
            color: "#ffffff",
            border: "none",
            borderRadius: "8px",
            fontWeight: "bold",
            fontSize: "1rem",
            cursor: "pointer",
            boxShadow: "0 4px 6px -1px rgba(99, 102, 241, 0.4)",
            marginBottom: "10px",
          }}
        >
          {loading ? "Connecting to Stripe..." : `Subscribe with Stripe (${selectedInterval === "yearly" ? "$39.99/yr" : "$4.99/mo"})`}
        </button>

        <div style={{ marginTop: "12px", textAlign: "center", borderTop: "1px solid #f3f4f6", paddingTop: "12px" }}>
          <p style={{ margin: "0 0 8px", fontSize: "0.75rem", color: "#9ca3af" }}>
            Development & Testing Mode:
          </p>
          <button
            type="button"
            onClick={handleSandboxActivate}
            disabled={loading}
            style={{
              background: "#f3f4f6",
              border: "1px dashed #9ca3af",
              borderRadius: "6px",
              padding: "6px 12px",
              fontSize: "0.78rem",
              color: "#4b5563",
              cursor: "pointer",
            }}
          >
            ⚡ Test Instantly: Simulate Pro Activation
          </button>
        </div>
      </div>
    </div>
  );
}
