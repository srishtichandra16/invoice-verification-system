-- Compatibility cleanup for databases created before brand-based tenancy.
-- The old company_id was supplied by customers and is no longer trusted or used.
ALTER TABLE IF EXISTS invoices DROP COLUMN IF EXISTS company_id;
ALTER TABLE IF EXISTS invoices ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
