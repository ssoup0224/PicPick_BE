package com.picpick.service;

import com.picpick.gemini.ChatRequest;
import com.picpick.gemini.ChatResponse;
import com.picpick.gemini.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatService chatService;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        chatService = new ChatService(chatClientBuilder);
    }

    @Test
    void chatTest() {
        // Given
        ChatRequest request = new ChatRequest("Hello");
        String expectedResponse = "Hi there!";

        when(chatClient.prompt()
                .user(anyString())
                .call()
                .content())
                .thenReturn(expectedResponse);

        // When
        ChatResponse response = chatService.chat(request);

        // Then
        assertEquals(expectedResponse, response.getResponse());
    }
}
