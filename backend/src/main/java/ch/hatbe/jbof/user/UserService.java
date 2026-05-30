package ch.hatbe.jbof.user;

import ch.hatbe.jbof.auth.entity.request.CreateUserRequest;
import ch.hatbe.jbof.core.exception.ResourceNotFoundException;
import ch.hatbe.jbof.user.entity.User;
import ch.hatbe.jbof.user.entity.dto.UserDetailDto;
import ch.hatbe.jbof.user.entity.dto.UserListDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDetailDto findDtoByKeycloakUserId(UUID id) {
        return this.userRepository.findUByKeycloakUserId(id)
                .map(this.userMapper::toDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("No user found!"));
    }

    public Page<UserListDto> findAll(Pageable pageable) {
        return this.userRepository.findAllUsers(pageable)
                .map(this.userMapper::toListDto);
    }

    public Optional<UserDetailDto> findById(UUID id) {
        return this.userRepository.findUserById(id)
                .map(this.userMapper::toDetailDto);
    }

    public UserListDto create(@Valid CreateUserRequest req, UUID keycloakId) {
        User user = new User();

        user.setKeycloakUserId(keycloakId);
        user.setUsername(req.username());
        user.setEmail(req.email());

        User savedUser = this.userRepository.save(user);

        return this.userMapper.toListDto(savedUser);
    }

    public boolean existsByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }
}
