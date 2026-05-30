package com.palak.workspace.organization;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByTenantId(String tenantId);

    Optional<Organization> findByOrganizationCode(String organizationCode);
}
