package com.ashapiro.catanserver.game.model;

import lombok.*;

import java.net.Socket;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class User {

    private Long id;

    private String name;

    private Boolean isReady;

    private Socket socket;
}
