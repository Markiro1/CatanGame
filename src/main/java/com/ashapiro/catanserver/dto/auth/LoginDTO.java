package com.ashapiro.catanserver.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotNull
        @NotBlank
        @Size(min = 1)
        String login,

        @NotNull
        @NotBlank
        @Size(min = 1)
        String password) {
}
