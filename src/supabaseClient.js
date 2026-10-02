import { createClient } from "@supabase/supabase-js";

// ============================================================================
// SUPABASE CONFIGURATION
// Replace the values below with your Supabase URL and Public Key.
// ============================================================================

// 1. Paste your Supabase Project URL here:
const SUPABASE_URL = "https://cxjenimwydepuqkwfiua.supabase.co/rest/v1/";

// 2. Paste your Supabase Public (Anon) API Key here:
const SUPABASE_PUBLIC_KEY = "sb_publishable_wR2T7czzgOxr2t9C6WuBsw_h2Ia67JA";

// Export the initialized Supabase client
export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLIC_KEY);
