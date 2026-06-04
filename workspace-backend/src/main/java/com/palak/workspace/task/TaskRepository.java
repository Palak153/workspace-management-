package com.palak.workspace.task;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {

    Optional<Task> findByTaskCode(String taskCode);

    List<Task> findByProjectCode(String projectCode);

    List<Task> findByTenantId(String tenantId);

    List<Task> findByAssignedToEmployeeId(String employeeId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByDueDate(LocalDate dueDate);

    long countByProjectCode(String projectCode);

    long countByProjectCodeAndStatus(String projectCode, TaskStatus status);

    List<Task> findByProjectCodeAndStatus(String projectCode, TaskStatus status);
}
