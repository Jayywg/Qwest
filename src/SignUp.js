import React, { useState } from "react";
import { supabase } from "./supabaseClient";

export default function SignUp() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setLoading(true);

    const { data, error } = await supabase.auth.signUp({
      email,
      password,
    });

    setLoading(false);

    if (error) {
      setErrorMessage(error.message);
    } else {
      // 1) Do NOT auto-login.
      // 2) Redirect to the Sign In page with email and registered status in query parameters
      const encodedEmail = encodeURIComponent(email);
      window.location.href = `/login?email=${encodedEmail}&registered=true`;
    }
  };

  return (
    <div className="auth-container">
      <form onSubmit={handleSubmit} className="auth-form">
        <h2>Sign Up</h2>

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
          {loading ? "Signing Up..." : "Sign Up"}
        </button>

        {/* Basic error handling if signup fails */}
        {errorMessage && (
          <p className="auth-error-message" style={{ color: "red", fontSize: "0.875rem", marginTop: "10px" }}>
            {errorMessage}
          </p>
        )}
      </form>
    </div>
  );
}
