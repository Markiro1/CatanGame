package com.ashapiro.catanserver.service.impl;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyRequestDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyResponseDto;
import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.repository.CustomLobbyRepository;
import com.ashapiro.catanserver.repository.LobbyRepository;
import com.ashapiro.catanserver.repository.UserRepository;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.service.UserToLobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LobbyServiceImpl implements LobbyService {

    private final LobbyRepository lobbyRepository;

    private final CustomLobbyRepository customLobbyRepository;

    private final UserRepository userRepository;

    private final UserToLobbyService userToLobbyService;

    @Transactional
    @Override
    public void joinToLobby(Long lobbyId, String token) {
        UserEntity user = userRepository.findUserByToken(token)
                .orElseThrow(() -> new NoSuchElementException("User does not exist with token: " + token));
        LobbyEntity lobby = lobbyRepository.findLobbyByIdFetchUserToLobby(lobbyId)
                .orElseThrow(() -> new NoSuchElementException("Lobby not found with id: " + lobbyId));
        userToLobbyService.createSession(user, lobby);
    }

    @Transactional
    @Override
    public CreateLobbyResponseDto createLobby(CreateLobbyRequestDto request) {
        String lobbyName = request.lobbyName();
        LobbyEntity lobby = new LobbyEntity();
        lobby.setName(lobbyName);
        lobbyRepository.save(lobby);
        return new CreateLobbyResponseDto(lobby.getId());
    }

    @Transactional
    @Override
    public Optional<LobbyEntity> removeUserFromLobby(UserEntity user) {
        LobbyEntity lobby = lobbyRepository.findLobbyByUser(user)
                .orElseThrow(() -> new NoSuchElementException("Lobby not found by user: " + user));
        userToLobbyService.deleteByUserId(user.getId());
        lobby.removeByUser(user);
        if (lobby.getUsersToLobby().isEmpty()) {
            lobbyRepository.delete(lobby);
        } else {
            Iterator<UserToLobby> iterator = lobby.getUsersToLobby().iterator();
            if (iterator.hasNext()) {
                UserToLobby userToLobby = iterator.next();
                userToLobby.setIsHost(true);
            }
        }
        return Optional.of(lobby);
    }

    @Override
    public List<AllLobbyDto> getAllLobbies() {
        return lobbyRepository.findAllLobbies();
    }

    @Override
    public LobbyDataDTO extractLobbyDetails(String token) {
        return customLobbyRepository.getLobbyDetailsByUserToken(token);
    }
}
