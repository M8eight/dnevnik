package com.rusobr.user.infrastructure.persistence.repository.projection;

public interface UserProjection {
    Long getId();
    String getFirstName();
    String getLastName();
    String getUsername();
    String getKeycloakId();
}
