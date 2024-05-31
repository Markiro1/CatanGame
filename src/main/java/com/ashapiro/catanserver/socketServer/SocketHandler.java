package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;

import java.net.Socket;

public interface SocketHandler {

    void onConnection(Socket socket);

    void onClose(Socket socket);

    <T extends SocketMessagePayload> void onMessage(Socket clientSocket, T message);
}
