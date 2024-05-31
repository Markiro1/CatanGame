package com.ashapiro.catanserver.repository.impl;

import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;
import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.repository.CustomLobbyRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional
public class CustomLobbyRepositoryImpl implements CustomLobbyRepository {

    private final EntityManager entityManager;

    @Override
    public LobbyDataDTO getLobbyDetailsByUserToken(String token) {
        LobbyDataDTO lobbyDataDTO = entityManager.createQuery(
                        "SELECT new com.ashapiro.catanserver.dto.lobby.LobbyDataDTO(l.id, l.name) FROM LobbyEntity l LEFT JOIN UserToLobby utl on utl.lobby.id = l.id " +
                                "join UserEntity u on utl.user.id = u.id where u.token = ?1",
                        LobbyDataDTO.class
                )
                .setParameter(1, token)
                .getSingleResult();

        List<SimpleUserDto> userList = entityManager.createQuery(
                        "SELECT new com.ashapiro.catanserver.dto.user.SimpleUserDto(u.id, u.username, utl.isHost) FROM UserEntity u LEFT JOIN UserToLobby utl on utl.user.id = u.id where utl.lobby.id = ?1",
                        SimpleUserDto.class
                )
                .setParameter(1, lobbyDataDTO.getLobbyId())
                .getResultList();

        System.out.println(userList);
        Long userId = entityManager.createQuery("select u.id from UserEntity u where u.token = ?1", Long.class)
                .setParameter(1, token)
                .getSingleResult();

        lobbyDataDTO.setUsers(userList);
        lobbyDataDTO.setUserIdWhoSendRequest(userId);
        return lobbyDataDTO;
    }
}
