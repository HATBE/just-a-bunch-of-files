package ch.hatbe.jbof.user.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDetailDto {
    @JsonProperty("id")
    private UUID userId;

    @JsonProperty("username")
    private String username;

    @Email
    @JsonProperty("email")
    private String email;
}
