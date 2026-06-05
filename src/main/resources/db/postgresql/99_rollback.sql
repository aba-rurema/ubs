-- =============================================================================
-- Utility Billing System - Rollback PostgreSQL Routines
-- =============================================================================

DROP TRIGGER IF EXISTS trg_bill_balance_after_update ON bills;
DROP TRIGGER IF EXISTS trg_bill_balance_before_update ON bills;
DROP TRIGGER IF EXISTS trg_bill_after_insert ON bills;

DROP FUNCTION IF EXISTS trg_fn_bill_balance_after_update();
DROP FUNCTION IF EXISTS trg_fn_bill_balance_before_update();
DROP FUNCTION IF EXISTS trg_fn_bill_after_insert();

DROP PROCEDURE IF EXISTS sp_mark_notification_failed(UUID, VARCHAR);
DROP PROCEDURE IF EXISTS sp_mark_notification_sent(UUID);
DROP PROCEDURE IF EXISTS sp_get_customer_outstanding_balance(UUID, NUMERIC, INTEGER);
DROP PROCEDURE IF EXISTS sp_process_bill_payment(UUID, NUMERIC, NUMERIC, VARCHAR);
DROP PROCEDURE IF EXISTS sp_create_payment_confirmation_notification(UUID, VARCHAR, NUMERIC, INTEGER, INTEGER, VARCHAR, UUID);
DROP PROCEDURE IF EXISTS sp_create_bill_issued_notification(UUID, VARCHAR, NUMERIC, INTEGER, INTEGER, DATE, VARCHAR, UUID);

DROP FUNCTION IF EXISTS fn_format_currency(NUMERIC);
DROP FUNCTION IF EXISTS fn_format_billing_period(INTEGER, INTEGER);
DROP FUNCTION IF EXISTS fn_create_notification(UUID, VARCHAR, VARCHAR, VARCHAR, VARCHAR);
