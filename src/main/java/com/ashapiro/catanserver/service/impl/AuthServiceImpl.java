package com.ashapiro.catanserver.service.impl;

import com.ashapiro.catanserver.dto.auth.LoginDTO;
import com.ashapiro.catanserver.dto.auth.RegisterDTO;
import com.ashapiro.catanserver.dto.jwt.JwtResponseDTO;
import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.service.AuthService;
import com.ashapiro.catanserver.service.UserService;
import com.ashapiro.catanserver.socketServer.SocketService;
import com.ashapiro.catanserver.userDetails.UserDetailsImpl;
import com.ashapiro.catanserver.util.JwtUtils;
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
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final SocketService socketService;

    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public SimpleUserDTO register(RegisterDTO registerDto) {
        return userService.save(registerDto);
    }

    @Transactional
    @Override
    public JwtResponseDTO createAuthToken(LoginDTO requestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.login(), requestDto.password())
            );
            UserEntity userEntity = modelMapper.map(authentication.getPrincipal(), UserEntity.class);
            socketService.removeUserFromLobbyIfPresent(userEntity.getToken());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String newToken = jwtUtils.generateToken((UserDetailsImpl) userDetails);
            jwtUtils.updateUserTokenByLogin(requestDto.login(), newToken);
            return new JwtResponseDTO(newToken);
        } catch (BadCredentialsException e) {
            throw e;
        }
    }
}
