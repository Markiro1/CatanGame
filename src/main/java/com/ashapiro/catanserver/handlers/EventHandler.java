package com.ashapiro.catanserver.handlers;

import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;

import java.net.Socket;

public interface EventHandler {
    <T extends SocketMessagePayload> void handle(T message, Socket clientSocket);
}
