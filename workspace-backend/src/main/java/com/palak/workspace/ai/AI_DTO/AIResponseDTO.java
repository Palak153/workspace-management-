package com.palak.workspace.ai.AI_DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIResponseDTO {
    private String riskLevel;

    private List<String> keyRisks;

    private String recommendation;
}
