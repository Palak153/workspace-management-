package com.palak.workspace.dashboard;

import lombok.Data;

@Data
public class DashboardSummaryDTO {

    private long totalProjects;

    private long plannedProjects;

    private long activeProjects;

    private long completedProjects;

    private long cancelledProjects;

    private long totalTasks;

    private long todoTasks;

    private long inProgressTasks;

    private long doneTasks;

    private long overdueTasks;

}
