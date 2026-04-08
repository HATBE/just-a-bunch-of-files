package ch.hatbe.jbof.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
public class User {
    @Id
    @UuidGenerator
    @Column(name = "user_id", nullable = false, updatable = false, length = 36)
    private UUID userId;

    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();;
}
