package com.multi_ai_agent_config.multi_agent_config.Records;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        Long id,
        String person,
        String type,
        String category,
        BigDecimal amount,
        String description,
        LocalDateTime occurredAt
) {}