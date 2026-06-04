package com.palak.workspace.project;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByProjectCode(String projectCode);

    List<Project> findByTenantId(String tenantId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByProjectManagerEmployeeId(String projectManagerEmployeeId);
}