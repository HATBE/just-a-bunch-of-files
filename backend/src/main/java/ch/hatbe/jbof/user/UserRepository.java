package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByOrderByCreatedAtDesc();

    Optional<User> findByUserId(UUID userId);

    Optional<User> findByKeycloakUserId(String keycloakUserId);
}
