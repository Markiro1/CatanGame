package com.ashapiro.catanserver.socketPayload.lobby;

import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@SuperBuilder
public class SocketResponseDisconnectFromLobbyPayload extends SocketMessagePayload {
    private String token;
}
