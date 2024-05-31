package com.ashapiro.catanserver.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.Socket;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Player {

    private Long id;

    private String name;

    private Boolean isReady;

    private Socket socket;
}
