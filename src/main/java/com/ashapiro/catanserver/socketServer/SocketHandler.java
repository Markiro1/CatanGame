package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;

import java.net.Socket;

public interface SocketHandler {

    void onConnection(Socket socket);

    void onClose(Socket socket);

    void onMessage(Socket clientSocket, SocketMessagePayload message);
}
