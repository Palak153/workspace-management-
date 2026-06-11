package com.palak.workspace;

import com.palak.workspace.notification.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTests {

    @Autowired
    private EmailService emailService;

    @Test
    public void sendTestEmail(){
        emailService.sendEmail(
                "palakjain.jain153@gmail.com",
                "Workspace Test",
                "Email integration successful");
    }
}
