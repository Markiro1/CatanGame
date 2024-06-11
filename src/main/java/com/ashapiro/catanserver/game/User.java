package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.game.enums.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private String name;

    private Boolean isReady;

    private Socket socket;

    private boolean hisTurn;

    private Map<Resource, Integer> inventory;

    public User(Long id, String name, Socket socket) {
        this.id = id;
        this.name = name;
        this.socket = socket;
        this.isReady = false;
        this.inventory = new HashMap<>();
    }
}
