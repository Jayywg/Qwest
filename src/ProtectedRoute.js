import React, { useEffect, useState } from "react";
import { supabase } from "./supabaseClient";

/**
 * ProtectedRoute: Wraps any private page or route.
 * Calls supabase.auth.getSession():
 * - If no active session exists, redirects to /login.
 * - If a session exists, renders the protected component/children.
 */
export default function ProtectedRoute({ children }) {
  const [loading, setLoading] = useState(true);
  const [hasSession, setHasSession] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const checkSession = async () => {
      const { data: { session }, error } = await supabase.auth.getSession();

      if (!isMounted) return;

      if (error || !session) {
        // No session: redirect to /login
        window.location.href = "/login";
      } else {
        setHasSession(true);
        setLoading(false);
      }
    };

    checkSession();

    return () => {
      isMounted = false;
    };
  }, []);

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
        <p>Loading session...</p>
      </div>
    );
  }

  return hasSession ? children : null;
}
