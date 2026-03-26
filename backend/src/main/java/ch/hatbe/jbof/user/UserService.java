package ch.hatbe.jbof.user;

import ch.hatbe.jbof.core.exception.NotFoundException;
import ch.hatbe.jbof.jooq.tables.records.UsersRecord;
import ch.hatbe.jbof.user.entity.UserDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public UserDtos.ListResponse create(UserDtos.CreateUserRequest request) {
        UsersRecord record = repository.create(request.username());
        return UserMapper.toListResponse(record);
    }

    public List<UserDtos.ListResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(UserMapper::toListResponse)
                .toList();
    }

    public UserDtos.ListResponse findById(UUID userId) {
        return repository.findById(userId)
                .map(UserMapper::toListResponse)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }
}
