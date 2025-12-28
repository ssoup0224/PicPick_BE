package com.picpick.controller;

import com.picpick.dto.ChatRequest;
import com.picpick.dto.ChatResponse;
import com.picpick.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST Controller for handling AI chat interactions.
 * Exposes endpoints for both standard (blocking) and streaming chat responses.
 */
@RestController // Marks this class as a REST controller where methods return data directly to
                // the body
@RequestMapping("/api/chat") // Base path for all endpoints in this controller
@RequiredArgsConstructor // Generates a constructor for all final fields (enabling constructor injection)
public class ChatController {

    /**
     * The business logic service for processing chat requests.
     */
    private final ChatService chatService;

    /**
     * Standard chat endpoint.
     * Takes a request and returns a single, complete AI response.
     *
     * @param request The chat request containing the user's message.
     * @return A ChatResponse object containing the single AI response.
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    /**
     * Streaming chat endpoint.
     * Returns the AI response as a stream of events (Server-Sent Events).
     * This is useful for providing a better user experience by displaying the
     * response as it is generated.
     *
     * @param request The chat request containing the user's message.
     * @return A Flux of strings, where each string represents a chunk of the AI's
     *         response.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatService.chatStream(request);
    }
}
