package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.MapGenerator;
import lombok.Getter;
import lombok.ToString;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@ToString
public class Lobby {

    private Long id;

    private ConcurrentHashMap<Socket, User> userMap;

    private CatanGame catanGame;

    public Lobby() {
    }

    public Lobby(Long id) {
        this.id = id;
        userMap = new ConcurrentHashMap<>();
    }

    public int startGame(List<Integer> map) {
        List<User> users = userMap.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        MapGenerator mapGenerator = new MapGenerator();
        mapGenerator.generateMap(map);
        int seed = mapGenerator.getSeed();

        catanGame = new CatanGame(
                users,
                mapGenerator.getHexes(),
                mapGenerator.getEdges(),
                mapGenerator.getVertices(),
                seed,
                this
        );

        return mapGenerator.getSeed();
    }
}
