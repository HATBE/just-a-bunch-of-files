package ch.hatbe.jbof.user;

import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.user.entity.UserDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    public UserDtos.ListResponse create(UserDtos.CreateUserRequest request) {
        return userMapper.toListResponse(repository.create(request.username()));
    }

    public List<UserDtos.ListResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(userMapper::toListResponse)
                .toList();
    }

    public UserDtos.DetailResponse findById(UUID userId) {
        return repository.findById(userId)
                .map(userMapper::toDetailResponse)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }
}
