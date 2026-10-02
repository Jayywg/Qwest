import React, { useState, useEffect } from "react";
import { supabase } from "./supabaseClient";

export default function SignIn() {
  // Read query params for pre-filling email and displaying success message
  const [email, setEmail] = useState(() => {
    if (typeof window !== "undefined") {
      return new URLSearchParams(window.location.search).get("email") || "";
    }
    return "";
  });

  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState(() => {
    if (typeof window !== "undefined") {
      const params = new URLSearchParams(window.location.search);
      return params.get("registered") === "true"
        ? "Your account has been created. Please check your email and verify your address before logging in."
        : "";
    }
    return "";
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const emailParam = params.get("email");
    const registeredParam = params.get("registered");

    if (emailParam) {
      setEmail(emailParam);
    }
    if (registeredParam === "true") {
      setSuccessMessage(
        "Your account has been created. Please check your email and verify your address before logging in."
      );
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setLoading(true);

    const { data, error } = await supabase.auth.signInWithPassword({
      email,
      password,
    });

    setLoading(false);

    if (error) {
      setErrorMessage(error.message);
    } else if (data?.session) {
      // Redirect to home/dashboard when a real session exists
      window.location.href = "/";
    } else {
      setErrorMessage("No active session found. Please verify your email or credentials.");
    }
  };

  return (
    <div className="auth-container">
      {/* Success message above the form when arriving from successful signup */}
      {successMessage && (
        <div
          className="auth-success-message"
          style={{
            backgroundColor: "#e8f5e9",
            color: "#2e7d32",
            border: "1px solid #c8e6c9",
            padding: "12px",
            borderRadius: "4px",
            marginBottom: "16px",
            fontSize: "0.875rem",
          }}
        >
          {successMessage}
        </div>
      )}

      <form onSubmit={handleSubmit} className="auth-form">
        <h2>Sign In</h2>

        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Signing In..." : "Sign In"}
        </button>

        {/* Error message */}
        {errorMessage && (
          <p className="auth-error-message" style={{ color: "red", fontSize: "0.875rem", marginTop: "10px" }}>
            {errorMessage}
          </p>
        )}
      </form>
    </div>
  );
}
