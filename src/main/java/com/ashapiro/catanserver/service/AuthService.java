package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.auth.LoginDTO;
import com.ashapiro.catanserver.dto.auth.RegisterDTO;
import com.ashapiro.catanserver.dto.jwt.JwtResponseDTO;
import com.ashapiro.catanserver.dto.user.SimpleUserDTO;

public interface AuthService {

    JwtResponseDTO createAuthToken(LoginDTO requestDto);

    SimpleUserDTO register(RegisterDTO registerDto);
}
