package com.ashapiro.catanserver.socketServer.payload.request;

import com.ashapiro.catanserver.game.enums.Resource;
import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@SuperBuilder
public class SocketRequestTradeResourcePayload extends SocketMessagePayload {

    private int requestedCountOfOutgoingResource;

    private Resource incomingResource;

    private Resource outgoingResource;
}
