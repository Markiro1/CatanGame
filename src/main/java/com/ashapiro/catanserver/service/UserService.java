package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.auth.RegisterDTO;
import com.ashapiro.catanserver.dto.user.SimpleUserDTO;
import com.ashapiro.catanserver.entity.UserEntity;

import java.util.Optional;

public interface UserService {

    SimpleUserDTO save(RegisterDTO registerDto);

    void updateUserTokenByLogin(String login, String token);

    Optional<UserEntity> findUserEntityByToken(String token);
}
