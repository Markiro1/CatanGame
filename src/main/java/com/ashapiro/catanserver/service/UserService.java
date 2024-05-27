package com.ashapiro.catanserver.service;

import com.ashapiro.catanserver.dto.auth.RegisterDto;
import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.entity.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserService {

    SimpleUserDto save(RegisterDto registerDto);

    void updateUserTokenByLogin(String login, String token);

    @Query("select u from User u left join fetch u.userToLobby")
    Optional<User> findUserByLogin(String login);

    Optional<User> findUserByToken(String token);

    List<String> retrieveTokensByLobbyId(Long lobbyId);

    Optional<SimpleUserDto> findSimpleUserByToken(String token);
}
