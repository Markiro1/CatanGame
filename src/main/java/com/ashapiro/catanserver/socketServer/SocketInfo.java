package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.game.model.Lobby;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;

@Getter @Setter
@AllArgsConstructor
public class SocketInfo {

    private Socket socket;

    private Lobby lobby;

    private String token;
}
