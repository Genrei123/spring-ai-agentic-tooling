package com.multi_ai_agent_config.multi_agent_config.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financeAssistantOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Assistant API")
                        .description("Spring AI + Ollama powered personal finance assistant. " +
                                "Ask questions about balance, spending, and transactions, " +
                                "backed by Postgres and answered via tool-calling LLM agent.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Genrey Cristobal")
                                .url("https://genrey.tech")));
    }
}