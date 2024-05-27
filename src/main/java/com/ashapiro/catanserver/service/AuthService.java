package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.auth.LoginDto;
import com.ashapiro.catanserver.dto.auth.RegisterDto;
import com.ashapiro.catanserver.dto.jwt.JwtResponseDto;
import com.ashapiro.catanserver.dto.user.SimpleUserDto;

public interface AuthService {

    JwtResponseDto createAuthToken(LoginDto requestDto);

    SimpleUserDto register(RegisterDto registerDto);
}
