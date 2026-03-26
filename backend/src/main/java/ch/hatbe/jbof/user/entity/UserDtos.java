package ch.hatbe.jbof.user.entity;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class UserDtos {
    public record CreateUserRequest(
            @NotBlank String username
    ) {}

    public record ListResponse(
            UUID userId,
            String username
    ) {}

    public record DetailResponse(
            UUID userId,
            String username
    ) {}
}
