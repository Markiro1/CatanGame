package com.ashapiro.catanserver.socketPayload.game;

import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SocketRequestStartGamePayload extends SocketMessagePayload {

    private List<Integer> map;
}
