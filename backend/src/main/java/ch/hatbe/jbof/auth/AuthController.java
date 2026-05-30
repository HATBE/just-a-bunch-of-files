package ch.hatbe.jbof.auth;

import ch.hatbe.jbof.auth.entity.request.CreateUserRequest;
import ch.hatbe.jbof.core.exception.ConflictException;
import ch.hatbe.jbof.user.UserService;
import ch.hatbe.jbof.user.entity.User;
import ch.hatbe.jbof.user.entity.dto.UserDetailDto;
import ch.hatbe.jbof.user.entity.dto.UserListDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    public UserDetailDto getMe(@AuthenticationPrincipal Jwt jwt) {
        return this.userService.findDtoByKeycloakUserId(UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserListDto> register(@Valid @RequestBody CreateUserRequest req) throws ConflictException, URISyntaxException {
        UserListDto user = this.authService.register(req);

        return ResponseEntity
                .created(new URI("/api/v1/users/" + user.getUserId()))
                .body(user);
    }
}
