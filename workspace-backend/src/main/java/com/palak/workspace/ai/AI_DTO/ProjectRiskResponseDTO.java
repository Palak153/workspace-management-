package com.palak.workspace.ai.AI_DTO;

import lombok.Data;

import java.util.List;

@Data
public class ProjectRiskResponseDTO {

    private String projectCode;

    private String projectName;

    private String riskLevel;

    private List<String> keyRisks;

    private String recommendation;
}
