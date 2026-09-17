package com.multi_ai_agent_config.multi_agent_config.Controllers;

import com.multi_ai_agent_config.multi_agent_config.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Tag(name = "Finance Chat", description = "Ask the AI assistant about your transactions and balance")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Ask a question, with conversation memory")
    @GetMapping("/ask")
    public String ask(
            @Parameter(example = "How much money does Genrey have left?") @RequestParam String question,
            @Parameter(description = "Conversation ID to group related turns together", example = "genrey-session-1")
            @RequestParam(defaultValue = "default-conversation") String conversationId) {
        return chatService.ask(question, conversationId);
    }

    @Operation(summary = "Ask a question with a streamed response, with conversation memory")
    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(
            @Parameter(example = "How much money does Genrey have left?") @RequestParam String question,
            @Parameter(description = "Conversation ID to group related turns together", example = "genrey-session-1")
            @RequestParam(defaultValue = "default-conversation") String conversationId) {
        return chatService.askStream(question, conversationId);
    }
}