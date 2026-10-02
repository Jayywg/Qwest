import React, { useState } from "react";
import { PET_CATALOG, ITEM_CATALOG } from "./gameCatalog";

export default function ProGameRewards({ isPro, onOpenUpgradeModal }) {
  const [activeTab, setActiveTab] = useState("pets"); // "pets" | "items"
  const [equippedPetId, setEquippedPetId] = useState("pet_forest_cat");
  const [equippedItems, setEquippedItems] = useState(["equip_iron_boots"]);

  const handleEquipPet = (pet) => {
    if (pet.isPro && !isPro) {
      onOpenUpgradeModal(`Unlock ${pet.name} and all other legendary companion pets with Pro!`);
      return;
    }
    setEquippedPetId(pet.id);
  };

  const handleEquipItem = (item) => {
    if (item.isPro && !isPro) {
      onOpenUpgradeModal(`Unlock ${item.name} and all other mythic equipment with Pro!`);
      return;
    }
    setEquippedItems((prev) =>
      prev.includes(item.id) ? prev.filter((id) => id !== item.id) : [...prev, item.id]
    );
  };

  return (
    <div
      style={{
        backgroundColor: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "20px",
        marginBottom: "24px",
        boxShadow: "0 1px 3px 0 rgba(0, 0, 0, 0.05)",
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
        <div>
          <h3 style={{ margin: "0 0 4px 0", fontSize: "1.15rem", color: "#111827" }}>
            Companion Pets & Mythic Items
          </h3>
          <p style={{ margin: 0, fontSize: "0.8rem", color: "#6b7280" }}>
            {isPro
              ? "⭐ Pro Active: All legendary companion pets & mythic items are unlocked!"
              : "Upgrade to Pro to unlock all legendary companion pets and mythic items."}
          </p>
        </div>

        {/* Tab Switcher */}
        <div style={{ display: "flex", gap: "6px", backgroundColor: "#f3f4f6", padding: "4px", borderRadius: "8px" }}>
          <button
            type="button"
            onClick={() => setActiveTab("pets")}
            style={{
              padding: "6px 12px",
              border: "none",
              borderRadius: "6px",
              fontSize: "0.8rem",
              fontWeight: activeTab === "pets" ? "bold" : "normal",
              backgroundColor: activeTab === "pets" ? "#ffffff" : "transparent",
              color: activeTab === "pets" ? "#4f46e5" : "#4b5563",
              cursor: "pointer",
              boxShadow: activeTab === "pets" ? "0 1px 2px rgba(0,0,0,0.05)" : "none",
            }}
          >
            🐾 Companion Pets
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("items")}
            style={{
              padding: "6px 12px",
              border: "none",
              borderRadius: "6px",
              fontSize: "0.8rem",
              fontWeight: activeTab === "items" ? "bold" : "normal",
              backgroundColor: activeTab === "items" ? "#ffffff" : "transparent",
              color: activeTab === "items" ? "#4f46e5" : "#4b5563",
              cursor: "pointer",
              boxShadow: activeTab === "items" ? "0 1px 2px rgba(0,0,0,0.05)" : "none",
            }}
          >
            ⚔️ Mythic Items
          </button>
        </div>
      </div>

      {/* Companion Pets Grid */}
      {activeTab === "pets" && (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: "12px" }}>
          {PET_CATALOG.map((pet) => {
            const isEquipped = equippedPetId === pet.id;
            const isLocked = pet.isPro && !isPro;

            return (
              <div
                key={pet.id}
                style={{
                  border: isEquipped
                    ? "2px solid #4f46e5"
                    : isLocked
                    ? "1px dashed #d1d5db"
                    : "1px solid #e5e7eb",
                  borderRadius: "10px",
                  padding: "14px",
                  backgroundColor: isLocked ? "#fafafa" : isEquipped ? "#f5f3ff" : "#ffffff",
                  position: "relative",
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "space-between",
                }}
              >
                {/* Pro Badge */}
                {pet.isPro && (
                  <span
                    style={{
                      position: "absolute",
                      top: "10px",
                      right: "10px",
                      backgroundColor: isPro ? "#10b981" : "#f59e0b",
                      color: "#ffffff",
                      fontSize: "0.65rem",
                      fontWeight: "bold",
                      padding: "2px 6px",
                      borderRadius: "9999px",
                    }}
                  >
                    {isPro ? "⭐ PRO UNLOCKED" : "🔒 PRO ONLY"}
                  </span>
                )}

                <div>
                  <div style={{ fontSize: "1.8rem", marginBottom: "6px" }}>{pet.icon}</div>
                  <h4 style={{ margin: "0 0 2px 0", fontSize: "0.95rem", color: "#111827" }}>
                    {pet.name}
                  </h4>
                  <div style={{ fontSize: "0.75rem", color: "#6366f1", fontWeight: "600", marginBottom: "4px" }}>
                    {pet.title}
                  </div>
                  <p style={{ margin: "0 0 6px 0", fontSize: "0.78rem", color: "#4b5563" }}>
                    {pet.description}
                  </p>
                  <div
                    style={{
                      fontSize: "0.72rem",
                      backgroundColor: "#f3f4f6",
                      padding: "4px 6px",
                      borderRadius: "4px",
                      color: "#374151",
                      marginBottom: "10px",
                    }}
                  >
                    ✨ {pet.ability}
                  </div>
                </div>

                {/* Action button */}
                <button
                  type="button"
                  onClick={() => handleEquipPet(pet)}
                  style={{
                    width: "100%",
                    padding: "8px",
                    border: "none",
                    borderRadius: "6px",
                    fontSize: "0.8rem",
                    fontWeight: "600",
                    cursor: "pointer",
                    backgroundColor: isLocked
                      ? "#f59e0b"
                      : isEquipped
                      ? "#4f46e5"
                      : "#f3f4f6",
                    color: isLocked || isEquipped ? "#ffffff" : "#374151",
                  }}
                >
                  {isLocked
                    ? "⭐ Unlock with Pro"
                    : isEquipped
                    ? "✓ Active Companion"
                    : "Equip Companion"}
                </button>
              </div>
            );
          })}
        </div>
      )}

      {/* Mythic Items Grid */}
      {activeTab === "items" && (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: "12px" }}>
          {ITEM_CATALOG.map((item) => {
            const isEquipped = equippedItems.includes(item.id);
            const isLocked = item.isPro && !isPro;

            return (
              <div
                key={item.id}
                style={{
                  border: isEquipped
                    ? "2px solid #4f46e5"
                    : isLocked
                    ? "1px dashed #d1d5db"
                    : "1px solid #e5e7eb",
                  borderRadius: "10px",
                  padding: "14px",
                  backgroundColor: isLocked ? "#fafafa" : isEquipped ? "#f5f3ff" : "#ffffff",
                  position: "relative",
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "space-between",
                }}
              >
                {/* Pro Badge */}
                {item.isPro && (
                  <span
                    style={{
                      position: "absolute",
                      top: "10px",
                      right: "10px",
                      backgroundColor: isPro ? "#10b981" : "#f59e0b",
                      color: "#ffffff",
                      fontSize: "0.65rem",
                      fontWeight: "bold",
                      padding: "2px 6px",
                      borderRadius: "9999px",
                    }}
                  >
                    {isPro ? "⭐ PRO UNLOCKED" : "🔒 PRO ONLY"}
                  </span>
                )}

                <div>
                  <div style={{ fontSize: "1.8rem", marginBottom: "6px" }}>{item.icon}</div>
                  <h4 style={{ margin: "0 0 2px 0", fontSize: "0.95rem", color: "#111827" }}>
                    {item.name}
                  </h4>
                  <div style={{ fontSize: "0.72rem", color: "#6b7280", fontWeight: "600", marginBottom: "4px" }}>
                    CATEGORY: {item.category}
                  </div>
                  <p style={{ margin: "0 0 6px 0", fontSize: "0.78rem", color: "#4b5563" }}>
                    {item.description}
                  </p>
                  <div
                    style={{
                      fontSize: "0.72rem",
                      backgroundColor: "#f3f4f6",
                      padding: "4px 6px",
                      borderRadius: "4px",
                      color: "#374151",
                      marginBottom: "10px",
                    }}
                  >
                    ⚡ {item.effect}
                  </div>
                </div>

                {/* Action button */}
                <button
                  type="button"
                  onClick={() => handleEquipItem(item)}
                  style={{
                    width: "100%",
                    padding: "8px",
                    border: "none",
                    borderRadius: "6px",
                    fontSize: "0.8rem",
                    fontWeight: "600",
                    cursor: "pointer",
                    backgroundColor: isLocked
                      ? "#f59e0b"
                      : isEquipped
                      ? "#4f46e5"
                      : "#f3f4f6",
                    color: isLocked || isEquipped ? "#ffffff" : "#374151",
                  }}
                >
                  {isLocked
                    ? "⭐ Unlock with Pro"
                    : isEquipped
                    ? "✓ Equipped"
                    : "Equip Item"}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
