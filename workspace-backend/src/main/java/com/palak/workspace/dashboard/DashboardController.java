package com.palak.workspace.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@RequestParam(required = false) String tenantId){
        return ResponseEntity.ok(dashboardService.getSummary(tenantId));
    }

    @GetMapping("/project-progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectProgressDTO>> getProjectProgress(@RequestParam(required = false) String tenantId){
        return ResponseEntity.ok(dashboardService.getProjectProgress(tenantId));
    }
}
