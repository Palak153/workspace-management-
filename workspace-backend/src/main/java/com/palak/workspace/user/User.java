package com.palak.workspace.user;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@NoArgsConstructor
@Data
public class User {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    private String organizationCode;

    private String firstName;
    private String lastName;

    @Email(message = "enter valid email")
    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed
    private UserRole role;

    private String designation;

    @Indexed(unique = true)
    private String employeeId;

    private Boolean isActive;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
