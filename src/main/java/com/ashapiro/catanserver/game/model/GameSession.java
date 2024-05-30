package com.ashapiro.catanserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class GameSession {

    private final List<Hex> hexes;

    private final List<Edge> edges;

    private final List<Vertex> vertices;

    private final List<Player> players;


    public boolean allPlayersIsReady() {
        return players.stream()
                .allMatch(player -> player.getIsReady() == true);
    }
}
