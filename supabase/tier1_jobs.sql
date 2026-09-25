-- Apply after complete_setup.sql. No authentication changes; server-only job queue.
BEGIN;
CREATE TABLE IF NOT EXISTS public.processing_jobs (
 id uuid PRIMARY KEY,
 owner_hash text NOT NULL CHECK (owner_hash ~ '^[0-9a-f]{64}$'),
 request_hash text NOT NULL,
 language text NOT NULL CHECK (language IN ('en','te','hi')),
 photo_path text NOT NULL,
 audio_path text NOT NULL,
 audio_suffix text NOT NULL,
 state text NOT NULL DEFAULT 'QUEUED' CHECK (state IN ('QUEUED','TRANSCRIBING','ENHANCING','ANALYZING','PRICING','GENERATING','COMPLETED','FAILED')),
 attempts integer NOT NULL DEFAULT 0,
 lease_token uuid,
 lease_until timestamptz,
 result jsonb,
 error jsonb,
 timings jsonb NOT NULL DEFAULT '{}',
 created_at timestamptz NOT NULL DEFAULT now(),
 updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS processing_jobs_claim_idx ON public.processing_jobs(state, lease_until, created_at);
CREATE INDEX IF NOT EXISTS processing_jobs_owner_idx ON public.processing_jobs(owner_hash, created_at);
ALTER TABLE public.processing_jobs ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.processing_jobs FROM anon, authenticated;
GRANT ALL ON public.processing_jobs TO service_role;

CREATE OR REPLACE FUNCTION public.kalasetu_claim_job(token uuid) RETURNS SETOF public.processing_jobs
LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE target uuid;
BEGIN
 UPDATE public.processing_jobs SET state='FAILED', error='{"code":"worker_interrupted","message":"Processing was interrupted repeatedly. Please create a new request."}', updated_at=now()
 WHERE state NOT IN ('COMPLETED','FAILED') AND attempts >= 3 AND lease_until < now();
 SELECT id INTO target FROM public.processing_jobs
 WHERE state NOT IN ('COMPLETED','FAILED') AND attempts < 3
 AND (lease_until IS NULL OR lease_until < now())
 ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT 1;
 RETURN QUERY UPDATE public.processing_jobs SET lease_token=token,
 lease_until=now()+interval '10 minutes', attempts=attempts+1,
 state='TRANSCRIBING', updated_at=now() WHERE id=target RETURNING *;
END $$;
REVOKE ALL ON FUNCTION public.kalasetu_claim_job(uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.kalasetu_claim_job(uuid) TO service_role;

INSERT INTO storage.buckets(id,name,public,file_size_limit)
VALUES ('processing-inputs','processing-inputs',false,10485760)
ON CONFLICT(id) DO UPDATE SET public=false,file_size_limit=10485760;
-- No anon/authenticated policies are granted on processing-inputs. Service role only.
CREATE OR REPLACE FUNCTION public.kalasetu_save(payload jsonb, owner text) RETURNS uuid
LANGUAGE plpgsql SECURITY DEFINER SET search_path = '' AS $$
DECLARE saved uuid;
BEGIN
  IF owner IS NULL OR owner !~ '^[0-9a-f]{64}$' THEN RAISE EXCEPTION 'Invalid owner'; END IF;
  IF coalesce(length(trim(payload->>'title')), 0) NOT BETWEEN 1 AND 80
     OR coalesce(length(trim(payload->>'description')), 0) NOT BETWEEN 1 AND 400
     OR coalesce((payload->>'final_price')::numeric, 0) NOT BETWEEN 0.01 AND 50000
     OR coalesce(payload->>'status', '') NOT IN ('saved','archived')
     OR coalesce(payload->>'category', '') NOT IN ('Pottery','Textiles','Bamboo','Wood','Home Decor','Jewellery','Paintings','Leather','Metalwork','Stone','Other')
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
COMMIT;
