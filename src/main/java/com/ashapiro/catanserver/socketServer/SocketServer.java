package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.util.MessageRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketServer implements CommandLineRunner {

    @Value("${socket.port}")
    private int port;

    private final SocketHandler socketHandler;

    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Socket server started on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            socketHandler.onConnection(clientSocket);

            byte[] buffer = new byte[4096];
            int num;
            while ((num = clientSocket.getInputStream().read(buffer)) != -1) {
                handleMessage(clientSocket, buffer, num);
            }
        } catch (IOException e) {
            log.error("Socket is closed.");
        } finally {
            socketHandler.onClose(clientSocket);
        }
    }

    private <T extends SocketMessagePayload> void handleMessage(Socket clientSocket, byte[] buffer, int num)
            throws JsonProcessingException {
        String requestJson = new String(buffer, 0, num);
        List<String> queries = Arrays.asList(requestJson.split("/nq"));
        for (String query : queries) {
            SocketMessagePayload socketMessage = objectMapper.readValue(query, SocketMessagePayload.class);
            EventType eventType = socketMessage.getEventType();
            T message = (T) objectMapper.readValue(query, MessageRegistry.getMessageClass(eventType));
            socketHandler.onMessage(clientSocket, message);
        }
    }
}
