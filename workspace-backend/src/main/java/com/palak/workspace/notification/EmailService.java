package com.palak.workspace.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(
            String to,
            String subject,
            String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendTaskAssignedEmail(String toEmail, String taskTitle, String projectName) {

        String subject = "New Task Assigned";

        String body = """
            Hello,

            A new task has been assigned to you.

            Task: %s
            Project: %s

            Please login to Workspace and review it.

            Regards,
            Workspace Team
            """.formatted(taskTitle, projectName);

        sendEmail(toEmail, subject, body);
    }

    public void sendTaskReAssignedEmail(String toEmail, String taskTitle, String projectName, String previousAssignee) {

        String subject = "Task Reassigned";

        String body = """
            Hello,

            An existing task has been reassigned to you.

            Task: %s
            Project: %s

            Previously assigned to: %s
            
            Please login to Workspace and review the task.

            Regards,
            Workspace Team
            """.formatted(taskTitle, projectName, previousAssignee);
        sendEmail(toEmail, subject, body);
    }

    public void sendTaskCompletedEmail(
            String toEmail,
            String taskTitle,
            String projectName) {

        String subject = "Task Completed";

        String body = """
            Hello,

            The following task has been marked as completed.

            Task: %s
            Project: %s

            Please login to Workspace if you would like to review the details.

            Regards,
            Workspace Team
            """.formatted(
                taskTitle,
                projectName
        );

        sendEmail(toEmail, subject, body);
    }

    public void sendTaskReminderEmail(String toEmail, String taskTitle, LocalDate dueDate) {

        String subject = "Task Due Reminder";

        String body = """
            Hello,

            This is a reminder that your task is due soon.

            Task: %s
            Due Date: %s

            Please complete it before the deadline.

            Regards,
            Workspace Team
            """.formatted(taskTitle, dueDate
        );
        sendEmail(toEmail, subject, body);
    }
}