package com.ashapiro.catanserver.socket;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.handlers.EventHandler;
import com.ashapiro.catanserver.handlers.EventHandlerFactory;
import com.ashapiro.catanserver.handlers.impl.DisconnectFromLobbyHandler;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.socketPayload.game.SocketRequestStartGamePayload;
import com.ashapiro.catanserver.socketPayload.lobby.SocketRequestConnectToLobbyPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SocketService implements CommandLineRunner {

    @Value("${socket.port}")
    private int port;

    private final EventHandlerFactory eventHandlerFactory;

    private final ObjectMapper objectMapper;

    private final Map<Socket, String> socketMap;

    private static Map<EventType, EventHandler> eventTypeEventHandlerMap = new HashMap<>();

    private static Map<EventType, Class<?>> typeMap = new HashMap<>();

    public SocketService(EventHandlerFactory eventHandlerFactory, ObjectMapper objectMapper, Map<Socket, String> socketMap) {
        this.eventHandlerFactory = eventHandlerFactory;
        this.objectMapper = objectMapper;
        this.socketMap = socketMap;

        eventTypeEventHandlerMap.put(EventType.REQUEST_CONNECT_TO_LOBBY, eventHandlerFactory.createHandler(EventType.REQUEST_CONNECT_TO_LOBBY));
        eventTypeEventHandlerMap.put(EventType.REQUEST_DISCONNECT_FROM_LOBBY, eventHandlerFactory.createHandler(EventType.REQUEST_DISCONNECT_FROM_LOBBY));
        eventTypeEventHandlerMap.put(EventType.REQUEST_START_GAME, eventHandlerFactory.createHandler(EventType.REQUEST_START_GAME));

        typeMap.put(EventType.REQUEST_CONNECT_TO_LOBBY, SocketRequestConnectToLobbyPayload.class);
        typeMap.put(EventType.REQUEST_START_GAME, SocketRequestStartGamePayload.class);
    }

    @Override
    public void run(String... args) {
        new Thread(this::startServer).start();
    }

    private void startServer() {
        log.info("Socket server starting...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Socket server already started.");
            while (true) {
                connectClient(serverSocket);
            }
        } catch (IOException e) {
            log.error("Error starting socket server.");
        }
    }

    private void connectClient(ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        socketMap.put(clientSocket, "");
        log.info("Client connected.");
        new Thread(() -> {
            handleClient(clientSocket);
        }).start();
    }

    private <T extends SocketMessagePayload> void handleClient(Socket clientSocket) {
        try {
            byte[] bucket = new byte[1024];
            int num;
            while ((num = clientSocket.getInputStream().read(bucket)) != -1) {
                StringBuilder json = new StringBuilder();
                for (int i = 0; i < num; i++) {
                    char c = (char) bucket[i];
                    json.append(c);
                }
                if (!json.isEmpty()) {
                    List<String> queries = Arrays.stream(json.toString().split("/nq")).collect(Collectors.toList());
                    for (String q : queries) {
                        SocketMessagePayload socketMessage = objectMapper.readValue(q, SocketMessagePayload.class);
                        EventType eventType = socketMessage.getEventType();
                        T message = (T) objectMapper.readValue(q, typeMap.get(eventType));
                        EventHandler eventHandler = eventTypeEventHandlerMap.get(eventType);
                        eventHandler.handle(message, clientSocket);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Socket is closed.");
        } finally {
            DisconnectFromLobbyHandler eventHandler =
                    (DisconnectFromLobbyHandler) eventTypeEventHandlerMap.get(EventType.REQUEST_DISCONNECT_FROM_LOBBY);
            eventHandler.handle(null, clientSocket);
            socketMap.remove(clientSocket);
        }
        log.info("Client disconnected.");
    }
}