package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.UserDetailDto;
import ch.hatbe.jbof.user.entity.UserListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserListDto> findAll() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UserMapper::toListDto)
                .toList();
    }

    public Optional<UserDetailDto> findById(UUID id) {
        return userRepository.findByUserId(id)
                .map(UserMapper::toDetailDto);
    }
}