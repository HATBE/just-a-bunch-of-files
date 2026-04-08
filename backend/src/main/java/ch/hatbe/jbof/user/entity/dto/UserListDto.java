package ch.hatbe.jbof.user.entity.dto;


import java.util.UUID;

public record UserListDto(
        UUID userId,
        String username
) {}