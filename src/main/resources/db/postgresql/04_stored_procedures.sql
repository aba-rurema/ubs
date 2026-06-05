-- =============================================================================
-- Utility Billing System - PostgreSQL Stored Procedures
-- =============================================================================

-- -----------------------------------------------------------------------------
-- sp_create_bill_issued_notification
-- Manually create a bill-issued notification for a customer.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_create_bill_issued_notification(
    IN  p_customer_id    UUID,
    IN  p_bill_number    VARCHAR(30),
    IN  p_total_amount   NUMERIC(12, 2),
    IN  p_billing_month  INTEGER,
    IN  p_billing_year   INTEGER,
    IN  p_due_date       DATE,
    IN  p_channel        VARCHAR(20) DEFAULT 'IN_APP',
    OUT p_notification_id UUID
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_title   VARCHAR(150);
    v_message VARCHAR(1000);
BEGIN
    v_title := 'Bill Issued - ' || fn_format_billing_period(p_billing_month, p_billing_year);

    v_message := 'Your bill ' || p_bill_number
        || ' for period ' || fn_format_billing_period(p_billing_month, p_billing_year)
        || ' has been issued. Total amount due: ' || fn_format_currency(p_total_amount)
        || '. Due date: ' || TO_CHAR(p_due_date, 'YYYY-MM-DD') || '.';

    p_notification_id := fn_create_notification(
        p_customer_id,
        v_title,
        v_message,
        'BILL_ISSUED',
        p_channel
    );
END;
$$;

-- -----------------------------------------------------------------------------
-- sp_create_payment_confirmation_notification
-- Manually create a payment confirmation notification for a customer.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_create_payment_confirmation_notification(
    IN  p_customer_id    UUID,
    IN  p_bill_number    VARCHAR(30),
    IN  p_total_amount   NUMERIC(12, 2),
    IN  p_billing_month  INTEGER,
    IN  p_billing_year   INTEGER,
    IN  p_channel        VARCHAR(20) DEFAULT 'IN_APP',
    OUT p_notification_id UUID
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_title   VARCHAR(150);
    v_message VARCHAR(1000);
BEGIN
    v_title := 'Payment Received - Bill Paid';

    v_message := 'Your bill ' || p_bill_number
        || ' for period ' || fn_format_billing_period(p_billing_month, p_billing_year)
        || ' has been fully paid. Total paid: ' || fn_format_currency(p_total_amount)
        || '. Thank you for your payment.';

    p_notification_id := fn_create_notification(
        p_customer_id,
        v_title,
        v_message,
        'PAYMENT_CONFIRMATION',
        p_channel
    );
END;
$$;

-- -----------------------------------------------------------------------------
-- sp_process_bill_payment
-- Updates bill payment totals, relies on triggers for PAID status + notification.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_process_bill_payment(
    IN  p_bill_id        UUID,
    IN  p_payment_amount NUMERIC(12, 2),
    OUT p_new_balance    NUMERIC(12, 2),
    OUT p_new_status     VARCHAR(20)
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_bill RECORD;
BEGIN
    IF p_payment_amount IS NULL OR p_payment_amount <= 0 THEN
        RAISE EXCEPTION 'Payment amount must be greater than zero';
    END IF;

    SELECT id, amount_paid, total_amount, balance, status
    INTO v_bill
    FROM bills
    WHERE id = p_bill_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Bill not found: %', p_bill_id;
    END IF;

    IF v_bill.status = 'PENDING' THEN
        RAISE EXCEPTION 'Bill must be approved before processing payments';
    END IF;

    IF v_bill.status = 'PAID' THEN
        RAISE EXCEPTION 'Bill is already fully paid';
    END IF;

    IF p_payment_amount > v_bill.balance THEN
        RAISE EXCEPTION 'Payment amount (%) exceeds outstanding balance (%)',
            p_payment_amount, v_bill.balance;
    END IF;

    UPDATE bills
    SET amount_paid = v_bill.amount_paid + p_payment_amount,
        balance     = v_bill.total_amount - (v_bill.amount_paid + p_payment_amount),
        updated_at  = NOW()
    WHERE id = p_bill_id
    RETURNING balance, status
    INTO p_new_balance, p_new_status;
END;
$$;

-- -----------------------------------------------------------------------------
-- sp_get_customer_outstanding_balance
-- Returns total outstanding balance across all unpaid bills for a customer.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_get_customer_outstanding_balance(
    IN  p_customer_id       UUID,
    OUT p_outstanding_total NUMERIC(12, 2),
    OUT p_unpaid_bill_count INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    SELECT COALESCE(SUM(balance), 0), COUNT(*)
    INTO p_outstanding_total, p_unpaid_bill_count
    FROM bills
    WHERE customer_id = p_customer_id
      AND status IN ('PENDING', 'PARTIALLY_PAID', 'APPROVED', 'OVERDUE')
      AND balance > 0;
END;
$$;

-- -----------------------------------------------------------------------------
-- sp_mark_notification_sent
-- Marks a notification as sent (utility for batch dispatch jobs).
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_mark_notification_sent(
    IN p_notification_id UUID
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE notifications
    SET status     = 'SENT',
        sent_at    = NOW(),
        updated_at = NOW(),
        failure_reason = NULL
    WHERE id = p_notification_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Notification not found: %', p_notification_id;
    END IF;
END;
$$;

-- -----------------------------------------------------------------------------
-- sp_mark_notification_failed
-- Marks a notification as failed with a reason.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_mark_notification_failed(
    IN p_notification_id UUID,
    IN p_failure_reason  VARCHAR(500)
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_failure_reason IS NULL OR BTRIM(p_failure_reason) = '' THEN
        RAISE EXCEPTION 'Failure reason is required';
    END IF;

    UPDATE notifications
    SET status         = 'FAILED',
        failure_reason = p_failure_reason,
        sent_at        = NULL,
        updated_at     = NOW()
    WHERE id = p_notification_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Notification not found: %', p_notification_id;
    END IF;
END;
$$;
