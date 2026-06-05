-- =============================================================================
-- Utility Billing System - PostgreSQL Trigger Functions
-- =============================================================================

-- Bill insert notification removed — Java sends BILL_APPROVED when finance approves.

-- -----------------------------------------------------------------------------
-- trg_fn_bill_balance_before_update
-- When balance becomes zero, set bill status to PAID.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_fn_bill_balance_before_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.balance IS NOT NULL
       AND NEW.balance <= 0
       AND (OLD.balance IS NULL OR OLD.balance > 0)
       AND OLD.status IS DISTINCT FROM 'PAID' THEN
        NEW.status     := 'PAID';
        NEW.balance    := 0;
        NEW.updated_at := NOW();
    END IF;

    RETURN NEW;
END;
$$;

-- -----------------------------------------------------------------------------
-- trg_fn_bill_balance_after_update
-- When balance becomes zero, create PAYMENT_CONFIRMATION notification.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_fn_bill_balance_after_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_title   VARCHAR(150);
    v_message VARCHAR(1000);
BEGIN
    IF NEW.balance IS NOT NULL
       AND NEW.balance <= 0
       AND (OLD.balance IS NULL OR OLD.balance > 0)
       AND NEW.status = 'PAID' THEN

        v_title := 'Payment Received - Bill Paid';

        v_message := 'Your bill ' || NEW.bill_number
            || ' for period ' || fn_format_billing_period(NEW.billing_month, NEW.billing_year)
            || ' has been fully paid. Total paid: ' || fn_format_currency(NEW.total_amount)
            || '. Thank you for your payment.';

        PERFORM fn_create_notification(
            NEW.customer_id,
            v_title,
            v_message,
            'PAYMENT_CONFIRMATION',
            'EMAIL'
        );
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_bill_after_insert ON bills;
DROP FUNCTION IF EXISTS trg_fn_bill_after_insert();
