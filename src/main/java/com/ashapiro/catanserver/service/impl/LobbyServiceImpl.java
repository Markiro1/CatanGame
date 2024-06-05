package com.ashapiro.catanserver.service.impl;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDTO;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyRequestDTO;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyResponseDTO;
import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;
import com.ashapiro.catanserver.entity.LobbyEntity;
import com.ashapiro.catanserver.entity.UserEntity;
import com.ashapiro.catanserver.entity.UserToLobby;
import com.ashapiro.catanserver.exceptions.rest.LobbyEntityNotFoundException;
import com.ashapiro.catanserver.exceptions.rest.UserEntityNotFoundException;
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
                .orElseThrow(() -> new UserEntityNotFoundException(token));
        LobbyEntity lobby = lobbyRepository.findLobbyByIdFetchUserToLobby(lobbyId)
                .orElseThrow(() -> new LobbyEntityNotFoundException(lobbyId));
        userToLobbyService.createSession(user, lobby);
    }

    @Transactional
    @Override
    public CreateLobbyResponseDTO createLobby(CreateLobbyRequestDTO request) {
        String lobbyName = request.lobbyName();
        LobbyEntity lobby = new LobbyEntity();
        lobby.setName(lobbyName);
        lobbyRepository.save(lobby);
        return new CreateLobbyResponseDTO(lobby.getId());
    }

    @Transactional
    @Override
    public Optional<LobbyEntity> removeUserEntityFromLobby(UserEntity user) {
        LobbyEntity lobby = lobbyRepository.findLobbyByUser(user)
                .orElseThrow(() -> new LobbyEntityNotFoundException(user));
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
    public List<AllLobbyDTO> getAllLobbies() {
        return lobbyRepository.findAllLobbies();
    }

    @Override
    public LobbyDataDTO extractLobbyDetails(String token) {
        return customLobbyRepository.getLobbyDetailsByUserToken(token);
    }
}
