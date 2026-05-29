package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUByKeycloakUserId(UUID keycloakUserId);
    boolean existsByKeycloakUserId(UUID keycloakUserId);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.userId = :id")
    Optional<User> findUserById(UUID id);
}
