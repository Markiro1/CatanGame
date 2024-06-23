package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.request.*;
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
    public void onMessage(Socket clientSocket, SocketMessagePayload message) {
        switch (message.getEventType()) {
            case REQUEST_CONNECT -> handleConnect(clientSocket, message);
            case REQUEST_START_GAME -> handleStartGame(clientSocket, message);
            case REQUEST_READY_AND_LOAD, REQUEST_USER_TURN_READY -> socketService.updateUserReadyStatus(clientSocket);

            case REQUEST_BUILD_SETTLEMENT -> handleBuildSettlement(clientSocket, message);
            case REQUEST_BUILD_ROAD -> handleBuildRoad(clientSocket, message);
            case REQUEST_BUILD_CITY -> handleBuildCity(clientSocket, message);

            case REQUEST_USE_KNIGHT_CARD -> handleUseKnightCard(clientSocket, message);
            case REQUEST_USE_MONOPOLY_CARD -> handleUseMonopolyCard(clientSocket, message);
            case REQUEST_USE_YEAR_OF_PLENTY_CARD -> handleUseYearOfPlentyCard(clientSocket, message);
            case REQUEST_USE_ROAD_BUILDING_CARD ->  socketService.useRoadBuildingCard(clientSocket);

            case REQUEST_USER_ROBBERY -> handleUserRobbery(clientSocket, message);

            case REQUEST_TRADE_RESOURCE -> handleTradeResource(clientSocket, message);
            case REQUEST_EXCHANGE_OFFER -> handleExchangeOffer(clientSocket, message);
            case REQUEST_EXCHANGE -> handleExchange(clientSocket, message);
            case REQUEST_BUY_CARD -> socketService.buyCard(clientSocket);
        }
    }

    private void handleExchange(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestExchangePayload) {
            SocketRequestExchangePayload socketMessage = (SocketRequestExchangePayload) message;
            socketService.exchange(clientSocket, socketMessage);
        }
    }

    private void handleExchangeOffer(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestExchangeResourcesOfferPayload) {
            SocketRequestExchangeResourcesOfferPayload socketMessage = (SocketRequestExchangeResourcesOfferPayload) message;
            socketService.exchangeResourcesOffer(clientSocket, socketMessage);
        }
    }

    private void handleUseYearOfPlentyCard(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestUseYearOfPlentyCardPayload) {
            SocketRequestUseYearOfPlentyCardPayload socketMessage = (SocketRequestUseYearOfPlentyCardPayload) message;
            socketService.useYearOfPlentyCard(clientSocket, socketMessage);
        }
    }

    private void handleUseMonopolyCard(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestUseMonopolyCardPayload) {
            SocketRequestUseMonopolyCardPayload socketMessage = (SocketRequestUseMonopolyCardPayload) message;
            socketService.useMonopolyCard(clientSocket, socketMessage);
        }
    }

    private void handleUseKnightCard(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestUseKnightCardPayload) {
            SocketRequestUseKnightCardPayload socketMessage = (SocketRequestUseKnightCardPayload) message;
            socketService.useKnightCard(clientSocket, socketMessage);
        }
    }

    private void handleUserRobbery(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestRobberyPayload) {
            SocketRequestRobberyPayload socketMessage = (SocketRequestRobberyPayload) message;
            socketService.userRobbery(clientSocket, socketMessage);
        }
    }

    private void handleTradeResource(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestTradeResourcePayload) {
            SocketRequestTradeResourcePayload socketMessage = (SocketRequestTradeResourcePayload) message;
            socketService.tradeResource(clientSocket, socketMessage);
        }
    }

    private  void handleConnect(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestConnectPayload) {
            SocketRequestConnectPayload socketMessage = (SocketRequestConnectPayload) message;
            socketService.connectToLobby(clientSocket, socketMessage);
        }
    }

    private void handleStartGame(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestStartGamePayload) {
            SocketRequestStartGamePayload socketMessage = (SocketRequestStartGamePayload) message;
            socketService.startGame(clientSocket, socketMessage);
        }
    }

    private void handleBuildSettlement(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestBuildPayload) {
            SocketRequestBuildPayload socketMessage = (SocketRequestBuildPayload) message;
            socketService.buildSettlement(clientSocket, socketMessage);
        }
    }

    private void handleBuildRoad(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestBuildPayload) {
            SocketRequestBuildPayload socketMessage = (SocketRequestBuildPayload) message;
            socketService.buildRoad(clientSocket, socketMessage);
        }
    }

    private void handleBuildCity(Socket clientSocket, SocketMessagePayload message) {
        if (message instanceof SocketRequestBuildPayload) {
            SocketRequestBuildPayload socketMessage = (SocketRequestBuildPayload) message;
            socketService.buildCity(clientSocket, socketMessage);
        }
    }
}
