CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              person VARCHAR(100) NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              category VARCHAR(50),
                              amount NUMERIC(12,2) NOT NULL,
                              description VARCHAR(255),
                              occurred_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_person ON transactions(person);
CREATE INDEX idx_transactions_occurred_at ON transactions(occurred_at);