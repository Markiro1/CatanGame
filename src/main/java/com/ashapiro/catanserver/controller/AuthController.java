package com.ashapiro.catanserver.controller;

import com.ashapiro.catanserver.dto.auth.LoginDTO;
import com.ashapiro.catanserver.dto.auth.RegisterDTO;
import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("catan-api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDto, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        return ResponseEntity
                .ok()
                .body(authService.createAuthToken(loginDto));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO registerDto,
                                      BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        SimpleUserDTO simpleUser = authService.register(registerDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(simpleUser);
    }
}
