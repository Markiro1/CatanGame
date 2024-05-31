package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.util.Lobby;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.Socket;

@Getter @Setter
@AllArgsConstructor
@ToString
public class SocketInfo {

    private Socket socket;

    private Lobby lobby;

    private String token;
}
