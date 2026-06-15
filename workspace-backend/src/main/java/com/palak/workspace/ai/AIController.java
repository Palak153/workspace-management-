package com.palak.workspace.ai;

import com.palak.workspace.ai.AI_DTO.ProjectRiskResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai-response")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/project-risk/{projectCode}")
    public ResponseEntity<ProjectRiskResponseDTO> calculateProjectRisk(@PathVariable String projectCode){
        return new ResponseEntity<>(aiService.analyzeProjectRisk(projectCode), HttpStatus.OK);
    }

}
