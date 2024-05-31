package com.ashapiro.catanserver.socketServer.payload.request;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SocketRequestStartGamePayload extends SocketMessagePayload {

    private List<Integer> numHexesInMapRow;
}
