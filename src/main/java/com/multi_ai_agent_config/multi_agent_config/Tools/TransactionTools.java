package com.multi_ai_agent_config.multi_agent_config.Tools;

import com.multi_ai_agent_config.multi_agent_config.Records.Transaction;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class TransactionTools {

    private final JdbcTemplate jdbc;

    public TransactionTools(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Transaction> TRANSACTION_ROW_MAPPER = (ResultSet rs, int rowNum) -> new Transaction(
            rs.getLong("id"),
            rs.getString("person"),
            rs.getString("type"),
            rs.getString("category"),
            rs.getBigDecimal("amount"),
            rs.getString("description"),
            rs.getObject("occurred_at", LocalDateTime.class)
    );

    @Tool(description = "Get the current remaining balance for a person: budget + income - expenses")
    public BigDecimal getBalance(@ToolParam(description = "Person's name") String person) {
        return jdbc.execute(
                "SELECT get_balance(?)",
                (PreparedStatement ps) -> {
                    ps.setString(1, person);
                    var rs = ps.executeQuery();
                    return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
                }
        );
    }

    @Tool(description = "Get the total budget set for a person")
    public BigDecimal getTotalBudget(@ToolParam(description = "Person's name") String person) {
        return jdbc.execute(
                "SELECT get_total_budget(?)",
                (PreparedStatement ps) -> {
                    ps.setString(1, person);
                    var rs = ps.executeQuery();
                    return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
                }
        );
    }

    @Tool(description = "Get total spending broken down by category for a person")
    public List<Map<String, Object>> getSpendingByCategory(
            @ToolParam(description = "Person's name") String person) {
        return jdbc.query(
                "SELECT * FROM get_spending_by_category(?)",
                ps -> ps.setString(1, person),
                (rs, rowNum) -> Map.of(
                        "category", rs.getString("category"),
                        "total", rs.getBigDecimal("total")
                )
        );
    }

    @Tool(description = "Get the N most recent transactions for a person")
    public List<Transaction> getRecentTransactions(
            @ToolParam(description = "Person's name") String person,
            @ToolParam(description = "How many recent transactions to return") int limit) {
        return jdbc.query(
                "SELECT * FROM get_recent_transactions(?, ?)",
                (PreparedStatement ps) -> {
                    ps.setString(1, person);
                    ps.setInt(2, limit);
                },
                TRANSACTION_ROW_MAPPER
        );
    }

    @Tool(description = "Record a new expense transaction for a person")
    public String addExpense(
            @ToolParam(description = "Person's name") String person,
            @ToolParam(description = "Expense amount in PHP") BigDecimal amount,
            @ToolParam(description = "Category, e.g. food, transport, bills") String category,
            @ToolParam(description = "Short description of the expense") String description) {

        jdbc.execute("{CALL add_expense(?, ?, ?, ?)}", (CallableStatementCallback<Void>) cs -> {
            cs.setString(1, person);
            cs.setBigDecimal(2, amount);
            cs.setString(3, category);
            cs.setString(4, description);
            cs.execute();
            return null;
        });

        return "Recorded expense of " + amount + " php for " + category;
    }
}