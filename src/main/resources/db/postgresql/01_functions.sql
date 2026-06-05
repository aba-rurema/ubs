-- =============================================================================
-- Utility Billing System - PostgreSQL Helper Functions
-- Run after Hibernate/JPA has created the schema (bills, notifications, customers)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- fn_create_notification
-- Shared helper used by triggers and stored procedures to insert notifications.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_create_notification(
    p_customer_id       UUID,
    p_title             VARCHAR(150),
    p_message           VARCHAR(1000),
    p_notification_type VARCHAR(30),
    p_channel           VARCHAR(20) DEFAULT 'IN_APP'
)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_notification_id UUID;
BEGIN
    IF p_customer_id IS NULL THEN
        RAISE EXCEPTION 'Customer ID is required to create a notification';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM customers WHERE id = p_customer_id) THEN
        RAISE EXCEPTION 'Customer not found: %', p_customer_id;
    END IF;

    v_notification_id := gen_random_uuid();

    INSERT INTO notifications (
        id,
        customer_id,
        title,
        message,
        notification_type,
        channel,
        status,
        sent_at,
        read_at,
        failure_reason,
        created_at,
        updated_at
    ) VALUES (
        v_notification_id,
        p_customer_id,
        p_title,
        p_message,
        p_notification_type,
        COALESCE(p_channel, 'IN_APP'),
        'PENDING',
        NULL,
        NULL,
        NULL,
        NOW(),
        NOW()
    );

    RETURN v_notification_id;
END;
$$;

-- -----------------------------------------------------------------------------
-- fn_format_billing_period
-- Formats billing month/year as MM/YYYY.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_format_billing_period(
    p_billing_month INTEGER,
    p_billing_year  INTEGER
)
RETURNS TEXT
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
    RETURN LPAD(p_billing_month::TEXT, 2, '0') || '/' || p_billing_year::TEXT;
END;
$$;

-- -----------------------------------------------------------------------------
-- fn_format_currency
-- Formats numeric amounts for notification messages.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_format_currency(p_amount NUMERIC)
RETURNS TEXT
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
    RETURN TO_CHAR(p_amount, 'FM999,999,990.00');
END;
$$;
