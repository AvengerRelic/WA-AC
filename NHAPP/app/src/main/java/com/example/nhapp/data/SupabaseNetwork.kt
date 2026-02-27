package com.example.nhapp.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime

object SupabaseNetwork {
    // TODO: Replace with your actual Supabase project credentials
    private const val SUPABASE_URL = "https://hxvjisxvzgujeimniquc.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh4dmppc3h2emd1amVpbW5pcXVjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzIwOTE2MDAsImV4cCI6MjA4NzY2NzYwMH0.3SH8qHO3_4ido6CwMLyOLM12WfZsjnBB7fADKweWIhk" // Your Anon Key

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }

    val auth get() = client.auth
    val db get() = client.postgrest
    val realtime get() = client.realtime
}
