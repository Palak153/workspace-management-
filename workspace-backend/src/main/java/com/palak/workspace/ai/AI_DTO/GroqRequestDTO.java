package com.palak.workspace.ai.AI_DTO;

import lombok.Data;

import java.util.List;

@Data
public class GroqRequestDTO {

    private String model;

    private List<Message> messages;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
