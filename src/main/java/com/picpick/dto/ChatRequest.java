package com.picpick.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for chat requests.
 * Used to encapsulate the message sent by the user to the AI.
 */
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor (required for JSON deserialization)
@AllArgsConstructor // Generates a constructor with all fields
public class ChatRequest {
    /**
     * The message text sent by the user.
     */
    private String message;
}
