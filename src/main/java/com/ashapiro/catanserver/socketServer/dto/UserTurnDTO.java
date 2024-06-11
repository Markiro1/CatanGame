package com.ashapiro.catanserver.socketServer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserTurnDTO {

    private Long userId;

    private Integer numOfTurn;
}
