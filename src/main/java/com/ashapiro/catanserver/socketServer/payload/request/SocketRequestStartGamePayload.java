package com.ashapiro.catanserver.socketServer.payload.request;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SocketRequestStartGamePayload implements SocketMessagePayload {

    private EventType eventType;

    private List<Integer> numHexesInMapRow;
}
