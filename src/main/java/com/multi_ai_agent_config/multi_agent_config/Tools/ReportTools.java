package com.multi_ai_agent_config.multi_agent_config.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ReportTools {

    private static final Logger log = LoggerFactory.getLogger(ReportTools.class);

    private final JdbcTemplate jdbc;

    // Directory the exported CSVs land in — served separately, see ReportController below
    private static final Path EXPORT_DIR = Path.of("exports");

    public ReportTools(JdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        Files.createDirectories(EXPORT_DIR);
    }

    @Tool(description = "Generate a downloadable CSV report of a person's transactions between two dates (inclusive). Dates must be in YYYY-MM-DD format.")
    public String exportTransactionsCsv(
            @ToolParam(description = "Person's name") String person,
            @ToolParam(description = "Start date, format YYYY-MM-DD") String startDate,
            @ToolParam(description = "End date, format YYYY-MM-DD") String endDate) {

        log.info("exportTransactionsCsv called with person='{}', startDate='{}', endDate='{}'", person, startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        String filename = "transactions_%s_%s_to_%s_%s.csv".formatted(
                person.toLowerCase(),
                startDate,
                endDate,
                UUID.randomUUID().toString().substring(0, 8)
        );
        Path filePath = EXPORT_DIR.resolve(filename);

        int rowCount = jdbc.query(
                "SELECT * FROM get_transactions_by_date_range(?, ?, ?)",
                (PreparedStatement ps) -> {
                    ps.setString(1, person);
                    ps.setObject(2, start);
                    ps.setObject(3, end);
                },
                (ResultSet rs) -> {
                    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
                        writer.println("id,type,category,amount,description,occurred_at");
                        int count = 0;
                        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        while (rs.next()) {
                            writer.printf("%d,%s,%s,%s,%s,%s%n",
                                    rs.getLong("id"),
                                    rs.getString("type"),
                                    csvSafe(rs.getString("category")),
                                    rs.getBigDecimal("amount"),
                                    csvSafe(rs.getString("description")),
                                    rs.getTimestamp("occurred_at").toLocalDateTime().format(fmt));
                            count++;
                        }
                        return count;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write CSV file", e);
                    }
                }
        );

        log.info("exportTransactionsCsv found {} row(s)", rowCount);

        if (rowCount == 0) {
            return "No transactions found for " + person + " between " + startDate + " and " + endDate + ". No file was created.";
        }

        return "Generated CSV report with " + rowCount + " transaction(s) for " + person +
                " from " + startDate + " to " + endDate + ". Download it at: /reports/download/" + filename;
    }

    // Basic CSV escaping — wraps in quotes if the value contains a comma, quote, or newline
    private String csvSafe(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}