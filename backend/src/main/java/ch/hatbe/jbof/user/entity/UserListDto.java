package ch.hatbe.jbof.user.entity;


import java.util.UUID;

public record UserListDto(
        UUID userId,
        String username
) {}