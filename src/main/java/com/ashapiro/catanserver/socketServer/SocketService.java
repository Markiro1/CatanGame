package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;

import java.net.Socket;

public interface SocketService {
    <T extends SocketMessagePayload> void connectToLobby(Socket clientSocket, T message);

    void disconnectFromLobby(Socket clientSocket);

    <T extends SocketMessagePayload> void startGame(Socket clientSocket, T message);

    void updatePlayerReadyStatus(Socket clientSocket);

    void removeUserFromLobbyIfPresent(String token);
}
