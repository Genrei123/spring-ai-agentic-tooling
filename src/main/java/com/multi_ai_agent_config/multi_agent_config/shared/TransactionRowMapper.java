package com.multi_ai_agent_config.multi_agent_config.shared;

import com.multi_ai_agent_config.multi_agent_config.Records.Transaction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.LocalDateTime;

public class TransactionRowMapper implements RowMapper<Transaction> {
    @Override
    public Transaction mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Transaction(
                rs.getLong("id"),
                rs.getString("person"),
                rs.getString("type"),
                rs.getString("category"),
                rs.getBigDecimal("amount"),
                rs.getString("description"),
                rs.getObject("occurred_at", LocalDateTime.class)
        );
    }
}
