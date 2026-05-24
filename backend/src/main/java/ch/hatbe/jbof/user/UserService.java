package ch.hatbe.jbof.user;

import ch.hatbe.jbof.core.exception.ResourceNotFoundException;
import ch.hatbe.jbof.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByKeycloakUserId(UUID id) {
        return this.userRepository.findByKeycloakUserId(id).orElseThrow(() -> new ResourceNotFoundException("Not User found!"));
    }
}
