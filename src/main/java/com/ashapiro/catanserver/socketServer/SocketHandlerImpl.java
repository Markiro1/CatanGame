package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestBuildPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestConnectPayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketServer.payload.request.SocketRequestTradeResourcePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketHandlerImpl implements SocketHandler {

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
            socketService.getSocketInfos().remove(clientSocket);
        } catch (IOException e) {
            log.error("Error close the socket: " + clientSocket.getInetAddress().getHostAddress());
        }
    }

    @Override
    public <T extends SocketMessagePayload> void onMessage(Socket clientSocket, T message) {
        switch (message.getEventType()) {
            case REQUEST_CONNECT -> handleConnect(clientSocket, message);
            case REQUEST_START_GAME -> handleStartGame(clientSocket, message);
            case REQUEST_READY_AND_LOAD, REQUEST_USER_TURN_READY -> socketService.updateUserReadyStatus(clientSocket);
            case REQUEST_BUILD_SETTLEMENT -> handleBuildSettlement(clientSocket, message);
            case REQUEST_BUILD_ROAD -> handleBuildRoad(clientSocket, message);
            case REQUEST_BUILD_CITY -> handleBuildCity(clientSocket, message);
            case REQUEST_TRADE_RESOURCE -> handleTradeResource(clientSocket, message);
        }
    }

    private <T extends SocketMessagePayload> void handleTradeResource(Socket clientSocket, T message) {
        if (message instanceof SocketRequestTradeResourcePayload) {
            SocketRequestTradeResourcePayload socketMessage = (SocketRequestTradeResourcePayload) message;
            socketService.tradeResource(clientSocket, socketMessage);
        }
    }

    private <T extends SocketMessagePayload> void handleConnect(Socket clientSocket, T message) {
        if (message instanceof SocketRequestConnectPayload) {
            SocketRequestConnectPayload socketMessage = (SocketRequestConnectPayload) message;
            socketService.connectToLobby(clientSocket, socketMessage);
        }
    }

    private <T extends SocketMessagePayload> void handleStartGame(Socket clientSocket, T message) {
        if (message instanceof SocketRequestStartGamePayload) {
            SocketRequestStartGamePayload socketMessage = (SocketRequestStartGamePayload) message;
            socketService.startGame(clientSocket, socketMessage);
        }
    }

    private <T extends SocketMessagePayload> void handleBuildSettlement(Socket clientSocket, T message) {
        if (message instanceof SocketRequestBuildPayload) {
            SocketRequestBuildPayload socketMessage = (SocketRequestBuildPayload) message;
            socketService.buildSettlement(clientSocket, socketMessage);
        }
    }

    private <T extends SocketMessagePayload> void handleBuildRoad(Socket clientSocket, T message) {
        if (message instanceof SocketRequestBuildPayload) {
            SocketRequestBuildPayload socketMessage = (SocketRequestBuildPayload) message;
            socketService.buildRoad(clientSocket, socketMessage);
        }
    }

    private <T extends SocketMessagePayload> void handleBuildCity(Socket clientSocket, T message) {
        if (message instanceof SocketRequestBuildPayload) {
            SocketRequestBuildPayload socketMessage = (SocketRequestBuildPayload) message;
            socketService.buildCity(clientSocket, socketMessage);
        }
    }
}
