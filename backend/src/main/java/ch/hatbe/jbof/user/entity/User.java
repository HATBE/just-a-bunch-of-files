package ch.hatbe.jbof.user.entity;

import ch.hatbe.jbof.core.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
public class User extends AuditableEntity {
    @Id
    @UuidGenerator
    @Column(name = "user_id", nullable = false, updatable = false, length = 36, unique = true)
    private UUID userId;

    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private UUID keycloakUserId;

    @Column(name = "username", nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "email", unique = true, length = 320)
    private String email;
}
