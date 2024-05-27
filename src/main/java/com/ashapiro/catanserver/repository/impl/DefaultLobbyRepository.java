package com.ashapiro.catanserver.repository.impl;

import com.ashapiro.catanserver.dto.lobby.LobbyDetailsDto;
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
public class DefaultLobbyRepository implements CustomLobbyRepository {

    private final EntityManager entityManager;

    @Override
    public LobbyDetailsDto getLobbyDetailsByUserToken(String token) {
        LobbyDetailsDto lobbyDetailsDto = entityManager.createQuery(
                        "SELECT new com.ashapiro.catanserver.dto.lobby.LobbyDetailsDto(l.id, l.name) FROM Lobby l LEFT JOIN UserToLobby utl on utl.lobby.id = l.id " +
                                "join User u on utl.user.id = u.id where u.token = ?1",
                        LobbyDetailsDto.class
                )
                .setParameter(1, token)
                .getSingleResult();

        List<SimpleUserDto> userList = entityManager.createQuery(
                        "SELECT new com.ashapiro.catanserver.dto.user.SimpleUserDto(u.id, u.username, utl.isHost) FROM User u LEFT JOIN UserToLobby utl on utl.user.id = u.id where utl.lobby.id = ?1",
                        SimpleUserDto.class
                )
                .setParameter(1, lobbyDetailsDto.getLobbyId())
                .getResultList();

        System.out.println(userList);
        Long userId = entityManager.createQuery("select u.id from User u where u.token = ?1", Long.class)
                .setParameter(1, token)
                .getSingleResult();

        lobbyDetailsDto.setUsers(userList);
        lobbyDetailsDto.setRequestUserId(userId);
        return lobbyDetailsDto;
    }
}
