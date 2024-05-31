package com.ashapiro.catanserver.socketServer.payload.response;

import com.ashapiro.catanserver.socketServer.payload.SocketMessagePayload;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@SuperBuilder
public class SocketResponseLoginPayload extends SocketMessagePayload {
    private String message;
}
