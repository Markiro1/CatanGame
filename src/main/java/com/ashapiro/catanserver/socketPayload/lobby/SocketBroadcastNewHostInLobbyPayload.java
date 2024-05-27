package com.ashapiro.catanserver.socketPayload.lobby;

import com.ashapiro.catanserver.dto.user.SimpleUserDto;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SocketBroadcastNewHostInLobbyPayload extends SocketMessagePayload {
    private String message;

    private SimpleUserDto user;
}
