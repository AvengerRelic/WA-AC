-- NHAPP Supabase Backend Schema & RLS Policies
-- Execute this entire file in the Supabase SQL Editor.

-- 1. Create Tables
CREATE TABLE public.users (
  id uuid REFERENCES auth.users NOT NULL PRIMARY KEY,
  name text,
  avatar_url text,
  created_at timestamp with time zone DEFAULT timezone('utc'::text, now()) NOT NULL
);

CREATE TABLE public.chats (
  id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
  created_at timestamp with time zone DEFAULT timezone('utc'::text, now()) NOT NULL
);

CREATE TABLE public.chat_members (
  chat_id uuid REFERENCES public.chats(id) ON DELETE CASCADE,
  user_id uuid REFERENCES public.users(id) ON DELETE CASCADE,
  PRIMARY KEY (chat_id, user_id)
);

CREATE TABLE public.messages (
  id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
  chat_id uuid REFERENCES public.chats(id) ON DELETE CASCADE NOT NULL,
  sender_id uuid REFERENCES public.users(id) ON DELETE CASCADE NOT NULL,
  content text NOT NULL,
  created_at timestamp with time zone DEFAULT timezone('utc'::text, now()) NOT NULL,
  read_at timestamp with time zone
);

-- 2. Create Indexes for Performance
CREATE INDEX idx_messages_chat_id_created_at ON public.messages(chat_id, created_at DESC);
CREATE INDEX idx_chat_members_user_id ON public.chat_members(user_id);

-- 3. Enable Row Level Security (RLS)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chat_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- 4. Create RLS Policies

-- Users: Anyone can view profiles, but only the owner can update their profile.
CREATE POLICY "Profiles are viewable by everyone" ON public.users FOR SELECT USING (true);
CREATE POLICY "Users can update own profile" ON public.users FOR UPDATE USING (auth.uid() = id);

-- Chats: Users can only see and create chats they are a part of.
CREATE POLICY "Users can view their chats" ON public.chats FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.chat_members WHERE chat_id = chats.id AND user_id = auth.uid())
);
CREATE POLICY "Users can create chats" ON public.chats FOR INSERT WITH CHECK (true);

-- Chat Members: Users can view members of their chats and add themselves.
CREATE POLICY "Users can view chat members" ON public.chat_members FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.chat_members AS cm WHERE cm.chat_id = chat_members.chat_id AND cm.user_id = auth.uid())
);
CREATE POLICY "Users can add members" ON public.chat_members FOR INSERT WITH CHECK (
  user_id = auth.uid() OR 
  EXISTS (SELECT 1 FROM public.chat_members WHERE chat_id = chat_members.chat_id AND user_id = auth.uid())
);

-- Messages: Users can only view and insert messages in chats they belong to.
CREATE POLICY "Users can view messages in their chats" ON public.messages FOR SELECT USING (
  EXISTS (SELECT 1 FROM public.chat_members WHERE chat_id = messages.chat_id AND user_id = auth.uid())
);
CREATE POLICY "Users can insert messages in their chats" ON public.messages FOR INSERT WITH CHECK (
  EXISTS (SELECT 1 FROM public.chat_members WHERE chat_id = messages.chat_id AND user_id = auth.uid()) AND sender_id = auth.uid()
);

-- 5. Set up Supabase Realtime
-- Enable realtime broadcasts for the messages table
alter publication supabase_realtime add table public.messages;

-- 6. Trigger to automatically create a public.users row on Auth signup
CREATE OR REPLACE FUNCTION public.handle_new_user() 
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.users (id, name, avatar_url)
  VALUES (new.id, new.raw_user_meta_data->>'name', null);
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();
