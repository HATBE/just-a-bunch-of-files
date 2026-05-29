package ch.hatbe.jbof.user;

import ch.hatbe.jbof.core.exception.ResourceNotFoundException;
import ch.hatbe.jbof.user.entity.User;
import ch.hatbe.jbof.user.entity.dto.UserDetailDto;
import ch.hatbe.jbof.user.entity.dto.UserListDto;
import lombok.RequiredArgsConstructor;
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

    public User findByKeycloakUserId(UUID id) {
        return this.userRepository.findUByKeycloakUserId(id).orElseThrow(() -> new ResourceNotFoundException("Not User found!"));
    }

    public List<UserListDto> findAll(Pageable pageable) {
        return this.userRepository.findAllUsers(pageable)
                .stream()
                .map(this.userMapper::toListDto)
                .toList();
    }

    public Optional<UserDetailDto> findById(UUID id) {
        return this.userRepository.findUserById(id).map(this.userMapper::toDetailDto);
    }
}
