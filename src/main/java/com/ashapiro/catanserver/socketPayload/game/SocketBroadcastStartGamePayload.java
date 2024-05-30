package com.ashapiro.catanserver.socketPayload.game;

import com.ashapiro.catanserver.game.dto.HexDto;
import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @Setter
@SuperBuilder
public class SocketBroadcastStartGamePayload extends SocketMessagePayload {
    private List<Integer> map;
    private List<HexDto> hexes;
}
