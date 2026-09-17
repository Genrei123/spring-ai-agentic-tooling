package com.multi_ai_agent_config.multi_agent_config.Config;

import com.multi_ai_agent_config.multi_agent_config.Tools.ReportTools;
import com.multi_ai_agent_config.multi_agent_config.Tools.TransactionTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

@Configuration
public class ChatClientConfig {

    private static String buildSystemPrompt() {
        return """
            You are a helpful personal finance assistant.
            Today's date is %s.

            CRITICAL RULE: You must NEVER write out transaction data, amounts, dates,
            or CSV content yourself. You do not have access to real financial data
            except through tool calls. Fabricating financial figures is strictly forbidden.

            CRITICAL RULE FOR CSV EXPORTS: When you call exportTransactionsCsv, the tool
            returns a message containing a download link. You must relay that exact
            message back to the user, including the exact download link text, without
            listing, inventing, or reformatting any transaction rows yourself. Do not
            print a CSV table in your reply — only mention the download link the tool gave you.

            When a date range is needed and the user doesn't specify exact dates,
            infer them relative to today's date above.
            If the user's name is not clear from the conversation, ask them for it
            rather than guessing or using a placeholder.
            Reply in a short, friendly sentence with the amount in PHP.
            """.formatted(LocalDate.now());
    }

    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
        JdbcChatMemoryRepository repository = JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .build();

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 TransactionTools transactionTools,
                                 ReportTools reportTools,
                                 ChatMemory chatMemory) {
        return builder
                .defaultSystem(buildSystemPrompt())
                .defaultTools(transactionTools, reportTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}