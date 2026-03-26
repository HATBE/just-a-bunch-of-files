package ch.hatbe.jbof.user;

import ch.hatbe.jbof.user.entity.UserDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDtos.ListResponse create(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<UserDtos.ListResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{userId}")
    public UserDtos.ListResponse findById(@PathVariable UUID userId) {
        return service.findById(userId);
    }
}