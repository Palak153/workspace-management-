package com.palak.workspace.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GroqConfig {

    @Value("${api-key}")
    private String apiKey;
}
