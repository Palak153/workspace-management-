package com.palak.workspace.ai.AI_DTO;

import lombok.Data;

@Data
public class ProjectMetricsDTO {

    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long overdueTasks;
    private long criticalPendingTasks;
}
