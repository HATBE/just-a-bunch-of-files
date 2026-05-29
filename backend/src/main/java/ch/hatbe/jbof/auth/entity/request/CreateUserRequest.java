package ch.hatbe.jbof.auth.entity.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest (
        @NotBlank(message = "Username must not be Empty")
        @NotNull(message = "Username must not be null")
        @Size(max = 32, min = 3, message = "Username must be between 3 and 32 characters!")
        String username,

        @NotBlank(message = "Email must not be Empty")
        @NotNull(message = "Email must not be null")
        @Size(max = 320, min = 5, message = "Email must be between 5 and 320 characters!")
        @Email(message = "Email must be valid")
        String email,

        @Size(max = 50, min = 1, message = "Firstname must be between 1 and 50 characters!")
        String firstname,

        @Size(max = 50, min = 1, message = "Lastname must be between 1 and 50 characters!")
        String lastname,

        @NotBlank(message = "Password must not be Empty")
        @NotNull(message = "Password must not be null")
        @Size(max = 1024, min = 6, message = "Password must be between 6 and 1024 characters!")
        String password
) {}
