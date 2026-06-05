-- =============================================================================
-- Utility Billing System - Install All PostgreSQL Routines
--
-- Prerequisites:
--   1. PostgreSQL 13+ (uses gen_random_uuid())
--   2. Application schema created (run Spring Boot once with ddl-auto=update)
--   3. Tables required: bills, notifications, customers
--
-- Usage (psql):
--   \i 01_functions.sql
--   \i 02_trigger_functions.sql
--   \i 03_triggers.sql
--   \i 04_stored_procedures.sql
--
-- Or from command line:
--   psql -U ubs_user -d ubs_db -f 01_functions.sql
--   psql -U ubs_user -d ubs_db -f 02_trigger_functions.sql
--   psql -U ubs_user -d ubs_db -f 03_triggers.sql
--   psql -U ubs_user -d ubs_db -f 04_stored_procedures.sql
-- =============================================================================

\echo 'Installing helper functions...'
\i 01_functions.sql

\echo 'Installing trigger functions...'
\i 02_trigger_functions.sql

\echo 'Installing triggers...'
\i 03_triggers.sql

\echo 'Installing stored procedures...'
\i 04_stored_procedures.sql

\echo 'PostgreSQL routines installed successfully.'
