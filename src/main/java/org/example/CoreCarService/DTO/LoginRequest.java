package org.example.CoreCarService.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Login request")
public record LoginRequest
    (
            @Schema(description = "User login", example = "user")
    @NotBlank String login,
            @Schema(description = "User password", example = "password")
    @NotBlank String password)
    {
}
