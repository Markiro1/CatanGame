package com.ashapiro.catanserver.dto.lobby;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class LobbyDetailsDto {

    private Long requestUserId;

    private Long lobbyId;

    private String lobbyName;

    private List<SimpleUserDto> users = new ArrayList<>();

    public LobbyDetailsDto(Long lobbyId, String lobbyName) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
    }
}
