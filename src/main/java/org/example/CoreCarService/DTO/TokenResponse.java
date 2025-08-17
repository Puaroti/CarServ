package org.example.CoreCarService.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Выдача токена")
public record TokenResponse
        (
                @Schema(description = "JWT token string")
                String token
        ) {
}

