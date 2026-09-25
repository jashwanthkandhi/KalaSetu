-- Apply after schema.sql. Additive migration: no existing product is deleted.
-- Existing unowned demo rows remain private until an administrator assigns ownership.
BEGIN;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS owner_hash text;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS public_profile jsonb NOT NULL DEFAULT '{}';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS market_data jsonb NOT NULL DEFAULT '{}';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS attributes jsonb NOT NULL DEFAULT '{}';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS language text NOT NULL DEFAULT 'en';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS image_warning boolean NOT NULL DEFAULT true;
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();
CREATE INDEX IF NOT EXISTS products_owner_idx ON public.products(owner_hash);
ALTER TABLE public.artisans ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS demo_read_all ON public.products;
DROP POLICY IF EXISTS demo_insert ON public.products;
REVOKE ALL ON public.products, public.artisans FROM anon, authenticated;

CREATE OR REPLACE VIEW public.marketplace_products AS
SELECT id, title, description, category, tags, original_image_url, enhanced_image_url,
       final_price, suggested_price, attributes, language, created_at, updated_at,
       jsonb_build_object('display_name', coalesce(public_profile->>'display_name', ''),
          'shop_name', coalesce(public_profile->>'shop_name', ''), 'craft', coalesce(public_profile->>'craft', ''),
          'location', coalesce(public_profile->>'location', ''), 'bio', coalesce(public_profile->>'bio', ''),
          'contact', CASE WHEN public_profile->>'contact_public' = 'true' THEN coalesce(public_profile->>'contact', '') ELSE '' END,
          'contact_public', coalesce(public_profile->>'contact_public' = 'true', false)) AS public_profile
FROM public.products WHERE status = 'saved' AND owner_hash IS NOT NULL;
GRANT SELECT ON public.marketplace_products TO anon, authenticated;

-- A 256-bit installation capability is hashed by the API; it is never exposed by views.
-- Atomic upsert also makes retries idempotent. A UUID is not proof of ownership.
CREATE OR REPLACE FUNCTION public.kalasetu_save(payload jsonb, owner text) RETURNS uuid
LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE saved uuid;
BEGIN
  IF owner IS NULL OR owner !~ '^[0-9a-f]{64}$' THEN RAISE EXCEPTION 'Invalid owner'; END IF;
  IF coalesce(length(trim(payload->>'title')), 0) NOT BETWEEN 1 AND 80
     OR coalesce(length(trim(payload->>'description')), 0) NOT BETWEEN 1 AND 400
     OR coalesce((payload->>'final_price')::numeric, 0) NOT BETWEEN 0.01 AND 50000
     OR coalesce(payload->>'status', '') NOT IN ('saved','archived')
     OR coalesce(payload->>'category', '') NOT IN ('Pottery','Textiles','Bamboo','Wood','Home Decor','Jewellery','Paintings','Leather','Other')
     OR coalesce(jsonb_array_length(payload->'tags'), 0) NOT BETWEEN 1 AND 12
     OR octet_length(payload::text) > 30000
  THEN RAISE EXCEPTION 'Invalid listing'; END IF;
  INSERT INTO public.products(id, owner_hash, title, description, category, tags,
    original_image_url, enhanced_image_url, voice_transcript, suggested_price, final_price,
    status, public_profile, market_data, attributes, language, image_warning)
  VALUES ((payload->>'product_id')::uuid, owner, payload->>'title', payload->>'description', payload->>'category',
    ARRAY(SELECT jsonb_array_elements_text(payload->'tags')), payload->>'original_image_url',
    payload->>'enhanced_image_url', payload->>'transcript', (payload->>'suggested_price')::numeric,
    (payload->>'final_price')::numeric, payload->>'status', payload->'public_profile',
    payload->'market_data', payload->'attributes', payload->>'language', (payload->>'image_warning')::boolean)
  ON CONFLICT (id) DO UPDATE SET title=EXCLUDED.title, description=EXCLUDED.description,
    category=EXCLUDED.category, tags=EXCLUDED.tags, final_price=EXCLUDED.final_price,
    suggested_price=EXCLUDED.suggested_price, status=EXCLUDED.status, public_profile=EXCLUDED.public_profile,
    market_data=EXCLUDED.market_data, attributes=EXCLUDED.attributes, language=EXCLUDED.language,
    original_image_url=EXCLUDED.original_image_url, enhanced_image_url=EXCLUDED.enhanced_image_url,
    voice_transcript=EXCLUDED.voice_transcript, image_warning=EXCLUDED.image_warning, updated_at=now()
  WHERE public.products.owner_hash = owner
  RETURNING id INTO saved;
  IF saved IS NULL THEN RAISE EXCEPTION 'Ownership mismatch'; END IF;
  RETURN saved;
END $$;
CREATE OR REPLACE FUNCTION public.kalasetu_catalog(owner text) RETURNS SETOF jsonb
LANGUAGE sql SECURITY DEFINER SET search_path = '' AS $$
 SELECT to_jsonb(p) - 'owner_hash' FROM public.products p
 WHERE p.owner_hash = owner AND owner ~ '^[0-9a-f]{64}$' ORDER BY p.created_at DESC LIMIT 1000;
$$;
CREATE OR REPLACE FUNCTION public.kalasetu_delete(product uuid, owner text) RETURNS boolean
LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
BEGIN
 IF owner IS NULL OR owner !~ '^[0-9a-f]{64}$' THEN RETURN false; END IF;
 IF EXISTS (SELECT 1 FROM public.products WHERE id = product AND owner_hash IS DISTINCT FROM owner) THEN RETURN false; END IF;
 DELETE FROM public.products WHERE id = product AND owner_hash = owner;
 RETURN true;
END $$;
REVOKE ALL ON FUNCTION public.kalasetu_save(jsonb,text), public.kalasetu_catalog(text), public.kalasetu_delete(uuid,text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.kalasetu_save(jsonb,text), public.kalasetu_catalog(text), public.kalasetu_delete(uuid,text) TO anon, authenticated;
COMMIT;

