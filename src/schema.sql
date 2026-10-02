-- ============================================================================
-- SUPABASE POSTGRESQL SCHEMA WITH ROW LEVEL SECURITY (RLS)
-- Run this script in your Supabase SQL Editor:
-- https://app.supabase.com/project/_/sql
-- ============================================================================

-- 1. Enable UUID extension if not already enabled
create extension if not exists "uuid-ossp";

-- ============================================================================
-- 1. QUESTS TABLE
-- Stores habit and quest tasks for each user.
-- ============================================================================
create table if not exists public.quests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade not null,
  title text not null,
  description text default '',
  category text default 'General',
  difficulty text default 'NORMAL', -- EASY, NORMAL, HARD, EPIC
  recurrence text default 'DAILY',  -- DAILY, WEEKLY, ONE_TIME
  start_date date default current_date,
  end_date date,
  reminder_time time,
  attachment_path text,
  is_archived boolean default false,
  created_at timestamptz default timezone('utc'::text, now()) not null
);

-- Enable Row Level Security (RLS)
alter table public.quests enable row level security;

-- Policies for Quests:
create policy "Users can view their own quests"
  on public.quests for select
  using (auth.uid() = user_id);

create policy "Users can insert their own quests"
  on public.quests for insert
  with check (auth.uid() = user_id);

create policy "Users can update their own quests"
  on public.quests for update
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can delete their own quests"
  on public.quests for delete
  using (auth.uid() = user_id);

-- ============================================================================
-- 2. QUEST COMPLETIONS TABLE
-- Records completions for calculating streaks, EXP, and gold rewards.
-- ============================================================================
create table if not exists public.quest_completions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade not null,
  quest_id uuid references public.quests(id) on delete cascade not null,
  date date not null default current_date,
  exp_awarded integer default 10,
  gold_awarded integer default 5,
  completed_at timestamptz default timezone('utc'::text, now()) not null,
  constraint unique_quest_user_date unique (user_id, quest_id, date)
);

-- Enable Row Level Security
alter table public.quest_completions enable row level security;

create policy "Users can view their own completions"
  on public.quest_completions for select
  using (auth.uid() = user_id);

create policy "Users can insert their own completions"
  on public.quest_completions for insert
  with check (auth.uid() = user_id);

create policy "Users can delete their own completions"
  on public.quest_completions for delete
  using (auth.uid() = user_id);

-- ============================================================================
-- 3. PROFILES TABLE
-- Stores gamification stats (level, exp, gold, streaks, character customization).
-- ============================================================================
create table if not exists public.profiles (
  id uuid references auth.users(id) on delete cascade primary key,
  character_name text default 'Adventurer',
  level integer default 1,
  exp integer default 0,
  gold integer default 0,
  current_streak integer default 0,
  longest_streak integer default 0,
  character_class text default 'Adventurer',
  hair_style text default 'Messy',
  hair_color text default 'Chestnut',
  skin_tone text default 'Fair',
  outfit_color text default 'Navy',
  accessory text default 'None',
  avatar_path text,
  updated_at timestamptz default timezone('utc'::text, now()) not null
);

-- Enable Row Level Security
alter table public.profiles enable row level security;

create policy "Users can view their own profile"
  on public.profiles for select
  using (auth.uid() = id);

create policy "Users can update their own profile"
  on public.profiles for update
  using (auth.uid() = id)
  with check (auth.uid() = id);

create policy "Users can insert their own profile"
  on public.profiles for insert
  with check (auth.uid() = id);

-- Auto-create profile trigger on auth.users insert
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, character_name)
  values (new.id, coalesce(new.raw_user_meta_data->>'full_name', 'Adventurer'))
  on conflict (id) do nothing;
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

-- ============================================================================
-- 4. INVENTORY TABLE
-- Stores equipment, potions, and items bought in the shop.
-- ============================================================================
create table if not exists public.inventory (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade not null,
  item_id text not null,
  quantity integer default 1,
  is_equipped boolean default false,
  updated_at timestamptz default timezone('utc'::text, now()) not null,
  constraint unique_user_item unique (user_id, item_id)
);

-- Enable Row Level Security
alter table public.inventory enable row level security;

create policy "Users can view their own inventory"
  on public.inventory for select
  using (auth.uid() = user_id);

create policy "Users can insert into their own inventory"
  on public.inventory for insert
  with check (auth.uid() = user_id);

create policy "Users can update their own inventory"
  on public.inventory for update
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can delete from their own inventory"
  on public.inventory for delete
  using (auth.uid() = user_id);

-- ============================================================================
-- 5. USER FILES TABLE (Storage Metadata for app-files)
-- Enforces per-user visibility for all files (quests, progression, etc.)
-- ============================================================================
create table if not exists public.user_files (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade not null,
  file_name text not null,
  file_path text not null,
  file_size bigint default 0,
  file_type text default '',
  category text default 'progression', -- 'quest', 'progression', 'general'
  created_at timestamptz default timezone('utc'::text, now()) not null
);

-- Enable Row Level Security (RLS)
alter table public.user_files enable row level security;

-- Policies: Each user can ONLY view, insert, and delete their own files
create policy "Users can view their own files"
  on public.user_files for select
  using (auth.uid() = user_id);

create policy "Users can insert their own files"
  on public.user_files for insert
  with check (auth.uid() = user_id);

create policy "Users can update their own files"
  on public.user_files for update
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can delete their own files"
  on public.user_files for delete
  using (auth.uid() = user_id);

-- ============================================================================
-- 6. NOTES TABLE (AI Notes SaaS with Plan Limit Enforcement)
-- ============================================================================
create table if not exists public.notes (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade not null,
  title text not null,
  content text default '',
  created_at timestamptz default timezone('utc'::text, now()) not null
);

-- Enable Row Level Security (RLS)
alter table public.notes enable row level security;

-- Policies ensuring users only access their own notes
create policy "Users can view their own notes"
  on public.notes for select
  using (auth.uid() = user_id);

create policy "Users can insert their own notes"
  on public.notes for insert
  with check (auth.uid() = user_id);

create policy "Users can update their own notes"
  on public.notes for update
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can delete their own notes"
  on public.notes for delete
  using (auth.uid() = user_id);

-- Database-Level Plan Limit Enforcement (Max 3 notes on Free Plan)
-- Prevents bypass even if users invoke Supabase REST API manually!
create or replace function public.check_free_plan_note_limit()
returns trigger as $$
declare
  note_count integer;
begin
  select count(*) into note_count
  from public.notes
  where user_id = new.user_id;

  if note_count >= 3 then
    raise exception 'Free plan limit reached. Upgrade to Pro to create unlimited notes.';
  end if;

  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists trigger_enforce_free_plan_note_limit on public.notes;
create trigger trigger_enforce_free_plan_note_limit
  before insert on public.notes
  for each row execute procedure public.check_free_plan_note_limit();

-- Enable Realtime for notes table so Total Notes count updates live
alter publication supabase_realtime add table public.notes;

-- ============================================================================
-- 7. STRIPE CUSTOMERS & SUBSCRIPTIONS TABLES (Pro Plan Gating)
-- Connects Stripe subscription backend to Supabase
-- ============================================================================

-- 7.1 Customers mapping table
create table if not exists public.customers (
  id uuid references auth.users(id) on delete cascade primary key,
  stripe_customer_id text unique not null,
  created_at timestamptz default timezone('utc'::text, now()) not null
);

alter table public.customers enable row level security;

create policy "Users can view their own customer record"
  on public.customers for select
  using (auth.uid() = id);

-- 7.2 Subscriptions table
create table if not exists public.subscriptions (
  id text primary key, -- Stripe subscription ID (e.g. sub_12345)
  user_id uuid references auth.users(id) on delete cascade not null,
  status text not null, -- 'active', 'trialing', 'past_due', 'canceled', etc.
  price_id text,
  plan_id text default 'pro',
  quantity integer default 1,
  cancel_at_period_end boolean default false,
  current_period_start timestamptz,
  current_period_end timestamptz,
  created_at timestamptz default timezone('utc'::text, now()) not null,
  ended_at timestamptz
);

alter table public.subscriptions enable row level security;

create policy "Users can view their own subscriptions"
  on public.subscriptions for select
  using (auth.uid() = user_id);

-- Realtime updates for subscription status
alter publication supabase_realtime add table public.subscriptions;

-- 7.3 Helper function to check if a user has an active Pro subscription
create or replace function public.is_user_pro(target_user_id uuid)
returns boolean as $$
begin
  return exists (
    select 1
    from public.subscriptions
    where user_id = target_user_id
      and status in ('active', 'trialing')
      and (current_period_end is null or current_period_end > now())
  );
end;
$$ language plpgsql security definer;

-- 7.4 Database-Level Quest Limit Enforcement for Free Users
-- Free users: max 3 active quests. Pro users: UNLIMITED quests!
create or replace function public.check_free_plan_quest_limit()
returns trigger as $$
declare
  active_quest_count integer;
  user_has_pro boolean;
begin
  -- 1. Check if user is Pro
  user_has_pro := public.is_user_pro(new.user_id);

  -- 2. If user is NOT Pro, enforce maximum 3 active quests
  if not user_has_pro then
    select count(*) into active_quest_count
    from public.quests
    where user_id = new.user_id
      and is_archived = false;

    if active_quest_count >= 3 then
      raise exception 'Free plan limit reached (max 3 quests). Upgrade to Pro to create unlimited quests and unlock pets & items.';
    end if;
  end if;

  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists trigger_enforce_free_plan_quest_limit on public.quests;
create trigger trigger_enforce_free_plan_quest_limit
  before insert on public.quests
  for each row execute procedure public.check_free_plan_quest_limit();



