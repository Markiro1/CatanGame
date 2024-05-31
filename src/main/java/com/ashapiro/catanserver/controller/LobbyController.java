package com.ashapiro.catanserver.controller;

import com.ashapiro.catanserver.dto.lobby.AllLobbyDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyRequestDto;
import com.ashapiro.catanserver.dto.lobby.CreateLobbyResponseDto;
import com.ashapiro.catanserver.dto.lobby.LobbyDataDTO;
import com.ashapiro.catanserver.service.LobbyService;
import com.ashapiro.catanserver.util.TokenManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("catan-api/lobbies")
@RequiredArgsConstructor
@Slf4j
public class LobbyController {

    private final LobbyService lobbyService;

    private final TokenManager tokenManager;

    @PostMapping("/lobby/join/{lobbyId}")
    public ResponseEntity<?> join(@PathVariable Long lobbyId, HttpServletRequest request) {
        String token = tokenManager.extractToken(request);
        lobbyService.joinToLobby(lobbyId, token);
        return ResponseEntity
                .ok()
                .body("Successfully join to lobby");
    }

    @GetMapping("/lobby/details")
    public ResponseEntity<LobbyDataDTO> getLobbyDetails(HttpServletRequest request) {
        String token = tokenManager.extractToken(request);
        return ResponseEntity
                .ok()
                .body(lobbyService.extractLobbyDetails(token));
    }

    @PostMapping("/create")
    public ResponseEntity<CreateLobbyResponseDto> create(@RequestBody CreateLobbyRequestDto createLobbyRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lobbyService.createLobby(createLobbyRequest));
    }

    @GetMapping("/all")
    public List<AllLobbyDto> getAll() {
        return lobbyService.getAllLobbies();
    }
}
