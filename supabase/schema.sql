-- Supabase Schema for KalaSetu MVP
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS artisans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT,
    phone TEXT UNIQUE,
    preferred_language TEXT DEFAULT 'te',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    artisan_id UUID REFERENCES artisans(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    tags TEXT[] DEFAULT '{}',
    original_image_url TEXT,
    enhanced_image_url TEXT,
    voice_transcript TEXT,
    suggested_price NUMERIC(10,2),
    final_price NUMERIC(10,2),
    status TEXT DEFAULT 'draft',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Row Level Security (RLS) Baseline (TH-9)
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'products' AND policyname = 'demo_read_all'
    ) THEN
        CREATE POLICY "demo_read_all" ON products FOR SELECT USING (true);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'products' AND policyname = 'demo_insert'
    ) THEN
        CREATE POLICY "demo_insert" ON products FOR INSERT WITH CHECK (true);
    END IF;
END
$$;
