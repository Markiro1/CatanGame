package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketHandlerImpl implements SocketHandler{

    private final SocketServiceImpl socketService;

    @Override
    public void onConnection(Socket clientSocket) {
        log.info("New client connected: " + clientSocket.getInetAddress().getHostAddress());
        SocketInfo socketInfo = new SocketInfo(clientSocket, null, null);
        socketService.getSocketInfos().add(socketInfo);
    }

    @Override
    public void onClose(Socket clientSocket) {
        socketService.disconnectFromLobby(clientSocket);
        try {
            clientSocket.close();
        } catch (IOException e) {
            log.error("Error close the socket: " + clientSocket.getInetAddress().getHostAddress());
        }
    }

    @Override
    public <T extends SocketMessagePayload> void onMessage(Socket clientSocket, T message) {
        switch (message.getEventType()) {
            case REQUEST_CONNECT -> socketService.connectToLobby(clientSocket, message);
            case REQUEST_START_GAME -> socketService.startGame(clientSocket, message);
            case REQUEST_READY_AND_LOAD -> socketService.updateUserReadyStatus(clientSocket);
            case REQUEST_BUILD_HOUSE -> socketService.buildHouse(clientSocket, message);
        }
    }
}
