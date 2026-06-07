-- Idempotent local database setup (safe to run more than once)
-- Used automatically by run-local.ps1 on first run

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'ubs_user') THEN
        CREATE ROLE ubs_user WITH LOGIN PASSWORD 'ubs_password';
    END IF;
END
$$;

-- CREATE DATABASE cannot run inside DO; run-local.ps1 creates ubs_db when missing
