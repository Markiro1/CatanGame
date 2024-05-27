package com.ashapiro.catanserver.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class SimpleUserDto{
    private Long id;

    @JsonIgnore
    private String login;

    private String name;

    private boolean isHost;

    public SimpleUserDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SimpleUserDto(Long id, String name, boolean isHost) {
        this.id = id;
        this.name = name;
        this.isHost = isHost;
    }
}
