package com.ashapiro.catanserver.socketServer;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import com.ashapiro.catanserver.socketServer.payload.request.DefaultSocketMessagePayload;
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
            StringBuilder message = new StringBuilder();
            while ((num = clientSocket.getInputStream().read(buffer)) != -1) {
                message.append(new String(buffer, 0, num));
                if (message.toString().contains("/nq")) {
                    handleMessage(clientSocket, message.toString());
                    message = new StringBuilder();
                }
            }
        } catch (IOException e) {
            log.error("Socket is closed.");
        } finally {
            socketHandler.onClose(clientSocket);
        }
    }

    private void handleMessage(Socket clientSocket, String message)
            throws JsonProcessingException {
        List<String> queries = Arrays.asList(message.split("/nq"));
        for (String query : queries) {
            try {
                DefaultSocketMessagePayload socketPayload = objectMapper.readValue(query, DefaultSocketMessagePayload.class);
                EventType eventType = socketPayload.getEventType();
                SocketMessagePayload socketMessage = objectMapper.readValue(query, MessageRegistry.getMessageClass(eventType));
                socketHandler.onMessage(clientSocket, socketMessage);
            } catch (Exception e) {
                System.out.println("ERROR PARSING TYPE " + e.getMessage());
            }
        }
    }
}
