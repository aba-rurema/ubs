-- =============================================================================
-- Utility Billing System - PostgreSQL Triggers
-- =============================================================================

-- Trigger 1 (bill insert notification) removed — notifications fire on bill approval in Java.

-- Trigger 2: When balance becomes zero, set status to PAID (BEFORE UPDATE)
DROP TRIGGER IF EXISTS trg_bill_balance_before_update ON bills;

CREATE TRIGGER trg_bill_balance_before_update
    BEFORE UPDATE OF balance, amount_paid ON bills
    FOR EACH ROW
    WHEN (OLD.balance IS DISTINCT FROM NEW.balance)
    EXECUTE PROCEDURE trg_fn_bill_balance_before_update();

-- Trigger 2: When balance becomes zero, create payment notification (AFTER UPDATE)
DROP TRIGGER IF EXISTS trg_bill_balance_after_update ON bills;

CREATE TRIGGER trg_bill_balance_after_update
    AFTER UPDATE OF balance, amount_paid ON bills
    FOR EACH ROW
    WHEN (
        OLD.balance IS DISTINCT FROM NEW.balance
        AND NEW.balance <= 0
        AND (OLD.balance IS NULL OR OLD.balance > 0)
    )
    EXECUTE PROCEDURE trg_fn_bill_balance_after_update();
