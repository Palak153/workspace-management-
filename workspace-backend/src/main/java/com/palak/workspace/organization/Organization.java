package com.palak.workspace.organization;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "organizations")
@NoArgsConstructor
@Data
public class Organization {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private String organizationName;

    @Indexed(unique = true)
    private String organizationCode;

    @Email(message = "enter valid email")
    private String email;

    private String phone;

    private String address;

    private OrganizationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
