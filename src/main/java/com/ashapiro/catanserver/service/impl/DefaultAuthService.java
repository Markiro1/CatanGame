package com.ashapiro.catanserver.service.impl;

import com.ashapiro.catanserver.dto.auth.LoginDto;
import com.ashapiro.catanserver.dto.auth.RegisterDto;
import com.ashapiro.catanserver.dto.jwt.JwtResponseDto;
import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.User;
import com.ashapiro.catanserver.service.AuthService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.userDetails.UserDetailsImpl;
import com.ashapiro.catanserver.util.JwtUtils;
import com.ashapiro.catanserver.util.LobbyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DefaultAuthService implements AuthService {

    private final JwtUtils jwtUtils;

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final LobbyUtils lobbyUtils;

    private final ModelMapper modelMapper;

    @Override
    public SimpleUserDto register(RegisterDto registerDto) {
        return userService.save(registerDto);
    }

    @Transactional
    @Override
    public JwtResponseDto createAuthToken(LoginDto requestDto) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.login(), requestDto.password())
            );
            User user = modelMapper.map(authentication.getPrincipal(), User.class);
            lobbyUtils.removeUserFromLobbyIfPresent(user);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String newToken = jwtUtils.generateToken((UserDetailsImpl) userDetails);
            jwtUtils.updateUserTokenByLogin(requestDto.login(), newToken);
            return new JwtResponseDto(newToken);
        } catch (BadCredentialsException e) {
            throw e;
        }
    }
}
