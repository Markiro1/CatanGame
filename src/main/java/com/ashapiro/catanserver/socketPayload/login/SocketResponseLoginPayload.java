package com.ashapiro.catanserver.socketPayload.login;

import com.ashapiro.catanserver.socketPayload.SocketMessagePayload;
import com.ashapiro.catanserver.enums.EventType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@SuperBuilder
public class SocketResponseLoginPayload extends SocketMessagePayload {
    private String message;
}
