package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.dto.HexDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter @Setter
public class SocketBroadcastStartGamePayload implements SocketBroadcastPayload {
    private EventType eventType;
    private List<Integer> numHexesInMapRow;
    private List<HexDto> hexes;
}
