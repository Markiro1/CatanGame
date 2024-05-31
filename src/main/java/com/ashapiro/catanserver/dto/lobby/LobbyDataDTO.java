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
public class LobbyDataDTO {

    private Long userIdWhoSendRequest;

    private Long lobbyId;

    private String lobbyName;

    private List<SimpleUserDto> users = new ArrayList<>();

    public LobbyDataDTO(Long lobbyId, String lobbyName) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
    }
}
