# NHAPP - Native Android WhatsApp Clone

A minimal, native Android WhatsApp clone using **Kotlin, Jetpack Compose, and MVVM** architecture. This project uses **Supabase** (Auth, Postgres, Realtime, Storage) as the Backend-as-a-Service, eliminating the need for a custom Node.js server.

## Features (MVP)
- **User Authentication**: Supabase Email OTP (Magic Link).
- **Real-time Chat**: 1:1 messaging using Supabase Realtime subscriptions.
- **Offline Notifications**: Firebase Cloud Messaging (FCM) integration.
- **Security**: Supabase Row Level Security (RLS) ensures users can only read/write their own chats.

---

## 🚀 Setup Instructions

### 1. Supabase Backend Setup
1. Create a new project at [Supabase.com](https://supabase.com).
2. Go to the **SQL Editor** and paste the contents of `supabase_schema.sql` (found in the root of this repository). Run the script to generate all required tables, indexes, and RLS policies.
3. In Supabase, go to **Authentication -> Providers** and ensure **Email** is enabled.
4. Go to **Project Settings -> API** and copy your `URL` and `anon public` keys.
5. Paste these keys into the `SupabaseNetwork.kt` file located at `app/src/main/java/com/example/nhapp/data/SupabaseNetwork.kt`.

### 2. Firebase Cloud Messaging (FCM) Setup
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an Android App with the package name: `com.example.nhapp`.
3. Download the `google-services.json` file.
4. Place `google-services.json` inside the `NHAPP/app/` directory of this Android project.
5. In Supabase, go to **Database -> Webhooks** (or Edge Functions) to trigger FCM pushes when a new row is inserted into the `messages` table and the recipient is offline.

### 3. Run the Android App
1. Open this `NHAPP` folder in **Android Studio** (Flamingo or Giraffe recommended).
2. Let Gradle sync the dependencies (Compose, Supabase-kt, Ktor, FCM).
3. Connect an Android Emulator or physical device (API 26+).
4. Click **Run 'app'**.

---

## Architecture Overview
- **Data Layer**: Supabase SDK models (`Models.kt`) and API Singletons (`SupabaseNetwork.kt`).
- **Domain Layer**: `ChatRepository.kt` & `AuthRepository.kt` wrap the Supabase data calls into Coroutine Flows.
- **UI Layer**: `MainActivity.kt` orchestrates Jetpack Compose Navigation between `AuthScreen`, `ChatListScreen`, and `ChatRoomScreen` using ViewModels.
