package com.ashapiro.catanserver.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @NotNull
    @NotBlank
    @Size(min = 1)
    private String username;

    @NotNull
    @NotBlank
    @Size(min = 1)
    private String login;

    @NotNull
    @NotBlank
    @Size(min = 1)
    private String password;
}
