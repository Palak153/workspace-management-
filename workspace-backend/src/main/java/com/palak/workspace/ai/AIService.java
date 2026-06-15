package com.palak.workspace.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palak.workspace.ai.AI_DTO.*;
import com.palak.workspace.common.exception.ValidationException;
import com.palak.workspace.project.Project;
import com.palak.workspace.project.ProjectService;
import com.palak.workspace.task.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AIService {

    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;
    private final GroqClient groqClient;

    public AIService(ProjectService projectService, TaskRepository taskRepository,
                     ObjectMapper objectMapper, GroqClient groqClient) {

        this.projectService = projectService;
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
        this.groqClient = groqClient;
    }


    private String buildRiskPrompt(Project project, ProjectMetricsDTO metrics) {

        return """
                You are a project management risk analysis system.
                
                Return ONLY valid JSON.
                
                Schema:
                
                {
                  "riskLevel": "",
                  "keyRisks": [],
                  "recommendation": ""
                }
                
                Rules:
                - riskLevel must be LOW, MEDIUM, or HIGH
                - keyRisks must contain actual risks from the project
                - recommendation must be specific to the project
                - no markdown
                - no explanation
                - return only JSON
                
                Project Name: %s
                Status: %s
                Progress: %d%%
                
                Total Tasks: %d
                Completed Tasks: %d
                In Progress Tasks: %d
                Overdue Tasks: %d
                High Priority Pending Tasks: %d
                """
                .formatted(
                        project.getProjectName(),
                        project.getStatus(),
                        project.getProgressPercentage(),
                        metrics.getTotalTasks(),
                        metrics.getCompletedTasks(),
                        metrics.getInProgressTasks(),
                        metrics.getOverdueTasks(),
                        metrics.getCriticalPendingTasks()
                );
    }

    private ProjectMetricsDTO calculateMetrics(List<Task> tasks){

        ProjectMetricsDTO calculatedMetrics = new ProjectMetricsDTO();

        calculatedMetrics.setTotalTasks(tasks.size());

        long completedTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        calculatedMetrics.setCompletedTasks(completedTasks);

        long inProgressTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS).count();
        calculatedMetrics.setInProgressTasks(inProgressTasks);

        long overdueTasks = tasks.stream().filter(task -> task.getStatus() != TaskStatus.DONE
                && task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())).count();
        calculatedMetrics.setOverdueTasks(overdueTasks);

        long highPriorityPendingTasks = tasks.stream().filter(task ->
                task.getPriority() == TaskPriority.CRITICAL && task.getStatus() != TaskStatus.DONE).count();
        calculatedMetrics.setCriticalPendingTasks(highPriorityPendingTasks);

        return calculatedMetrics;
    }

    @Cacheable(value = "projectRiskAnalysis", key = "#projectCode")
    public ProjectRiskResponseDTO analyzeProjectRisk(String projectCode) {
        Project project = projectService.findByProjectCode(projectCode);
        List<Task> tasks = taskRepository.findByProjectCode(projectCode);

        ProjectMetricsDTO metrics = calculateMetrics(tasks);
        String prompt = buildRiskPrompt(project, metrics);

        log.info("Generated AI Prompt");

        String aiAnalysis = groqClient.generate(prompt);

        try{
            AIResponseDTO analysis = objectMapper.readValue(aiAnalysis, AIResponseDTO.class);

            ProjectRiskResponseDTO dto = new ProjectRiskResponseDTO();

            dto.setProjectCode(projectCode);
            dto.setProjectName(project.getProjectName());
            dto.setRiskLevel(analysis.getRiskLevel());
            dto.setKeyRisks(analysis.getKeyRisks());
            dto.setRecommendation(analysis.getRecommendation());
            return dto;
        }catch (Exception e){
            log.error("Failed to parse AI response", e);
            throw new ValidationException("Unable to generate project risk analysis");
        }
    }
}
