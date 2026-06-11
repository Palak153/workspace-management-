package com.palak.workspace.notification.scheduler;

import com.palak.workspace.notification.EmailService;
import com.palak.workspace.task.Task;
import com.palak.workspace.task.TaskRepository;
import com.palak.workspace.task.TaskStatus;
import com.palak.workspace.user.User;
import com.palak.workspace.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public TaskSchedulerService(TaskRepository taskRepository, UserRepository userRepository, EmailService emailService) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendTaskReminders() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Task> tasks = taskRepository.findByDueDateAndStatusNot(tomorrow, TaskStatus.DONE);

        for(Task task : tasks){

            User assignee = userRepository.findByEmployeeId(task.getAssignedToEmployeeId()).orElse(null);

            if(assignee == null){
                continue;
            }

            emailService.sendTaskReminderEmail(assignee.getEmail(), task.getTitle(), task.getDueDate()
            );
        }

        log.info(
                "Task reminder scheduler executed. Tasks processed: {}", tasks.size()
        );
    }
}
