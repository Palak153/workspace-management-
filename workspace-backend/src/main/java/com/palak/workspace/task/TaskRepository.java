package com.palak.workspace.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {

    Optional<Task> findByTaskCode(String taskCode);

    List<Task> findByProjectCode(String projectCode);

    List<Task> findByTenantId(String tenantId);

    List<Task> findByAssignedToEmployeeId(String employeeId);

    List<Task> findByDueDateAndStatusNot(LocalDate dueDate, TaskStatus status);

    long countByProjectCode(String projectCode);

    long countByProjectCodeAndStatus(String projectCode, TaskStatus status);

    List<Task> findByProjectCodeAndStatus(String projectCode, TaskStatus status);

    List<Task> findByProjectCodeIn(List<String> projectCodes);

    Page<Task> findByTenantId(String tenantId, Pageable pageable);
    Page<Task> findByAssignedToEmployeeId(String employeeId, Pageable pageable);


}
