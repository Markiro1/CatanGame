package com.ashapiro.catanserver.socketServer.payload.broadcast;

import com.ashapiro.catanserver.enums.EventType;
import com.ashapiro.catanserver.game.dto.HexDTO;
import com.ashapiro.catanserver.socketServer.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter @Setter
@ToString
public class SocketBroadcastStartGamePayload implements SocketBroadcastPayload {

    private EventType eventType;

    private List<Integer> hexesInRowCounts;

    private int seed;

    private List<UserDTO> users;

    private UserDTO currentUser;
}
