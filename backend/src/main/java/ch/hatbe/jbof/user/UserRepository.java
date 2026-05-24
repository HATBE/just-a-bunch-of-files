package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByKeycloakUserId(UUID keycloakUserId);
    boolean existsByUsernameIgnoreCase(String username);
}
