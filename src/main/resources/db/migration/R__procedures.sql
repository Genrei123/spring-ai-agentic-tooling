-- ============================================
-- FUNCTIONS (return values, called via SELECT)
-- ============================================

CREATE OR REPLACE FUNCTION get_balance(p_person VARCHAR)
RETURNS NUMERIC AS $$
SELECT
    COALESCE(SUM(CASE WHEN type IN ('BUDGET','INCOME') THEN amount ELSE 0 END), 0)
        -
    COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0)
FROM transactions
WHERE person = p_person;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION get_total_budget(p_person VARCHAR)
RETURNS NUMERIC AS $$
SELECT COALESCE(SUM(amount), 0)
FROM transactions
WHERE person = p_person AND type = 'BUDGET';
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION get_spending_by_category(p_person VARCHAR)
RETURNS TABLE(category VARCHAR, total NUMERIC) AS $$
SELECT category, SUM(amount) AS total
FROM transactions
WHERE person = p_person AND type = 'EXPENSE'
GROUP BY category
ORDER BY total DESC;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION get_recent_transactions(p_person VARCHAR, p_limit INT)
RETURNS SETOF transactions AS $$
SELECT *
FROM transactions
WHERE person = p_person
ORDER BY occurred_at DESC
    LIMIT p_limit;
$$ LANGUAGE sql STABLE;

-- ============================================
-- PROCEDURES (side effects, called via CALL)
-- ============================================

CREATE OR REPLACE PROCEDURE add_expense(
    p_person VARCHAR,
    p_amount NUMERIC,
    p_category VARCHAR,
    p_description VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
INSERT INTO transactions (person, type, category, amount, description)
VALUES (p_person, 'EXPENSE', p_category, p_amount, p_description);
END;
$$;

-- ============================================
-- PROCEDURES for CSV specific purposes
-- ============================================

CREATE OR REPLACE FUNCTION get_transactions_by_date_range(
    p_person VARCHAR,
    p_start_date DATE,
    p_end_date DATE
)
RETURNS SETOF transactions AS $$
SELECT *
FROM transactions
WHERE person = p_person
  AND occurred_at::date BETWEEN p_start_date AND p_end_date
ORDER BY occurred_at;
$$ LANGUAGE sql STABLE;