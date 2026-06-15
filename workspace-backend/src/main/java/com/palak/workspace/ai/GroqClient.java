package com.palak.workspace.ai;

import com.palak.workspace.ai.AI_DTO.GroqRequestDTO;
import com.palak.workspace.ai.AI_DTO.GroqResponseDTO;
import com.palak.workspace.config.GroqConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GroqClient {

    private final RestTemplate restTemplate;
    private final GroqConfig groqConfig;

    public GroqClient(RestTemplate restTemplate, GroqConfig groqConfig) {

        this.restTemplate = restTemplate;
        this.groqConfig = groqConfig;
    }

    public String generate(String prompt) {

        GroqRequestDTO.Message message = new GroqRequestDTO.Message();
        message.setRole("user");
        message.setContent(prompt);

        GroqRequestDTO request = new GroqRequestDTO();
        request.setModel("llama-3.1-8b-instant");
        request.setMessages(List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GroqRequestDTO> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<GroqResponseDTO> response = restTemplate.exchange(
                        "https://api.groq.com/openai/v1/chat/completions",
                        HttpMethod.POST,
                        entity,
                        GroqResponseDTO.class);

        return cleanResponse(response.getBody()
                        .getChoices()
                        .get(0)
                        .getMessage()
                        .getContent());
    }

    private String cleanResponse(String response) {
        return response
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}
