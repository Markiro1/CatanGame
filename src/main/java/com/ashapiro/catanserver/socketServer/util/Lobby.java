package com.ashapiro.catanserver.socketServer.util;

import com.ashapiro.catanserver.game.CatanGame;
import com.ashapiro.catanserver.game.MapGenerator;
import com.ashapiro.catanserver.game.dto.HexDTO;
import com.ashapiro.catanserver.game.model.User;
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

    public Lobby(Long id) {
        this.id = id;
        this.userMap = new ConcurrentHashMap<>();
    }

    public List<HexDTO> startGame(List<Integer> map) {
        List<User> users = userMap.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        MapGenerator mapGenerator = new MapGenerator();
        mapGenerator.generateMap(map);
        catanGame = new CatanGame(
                users,
                mapGenerator.getHexes(),
                mapGenerator.getEdges(),
                mapGenerator.getVertices()
        );

        return mapGenerator.getHexes()
                .stream()
                .map(hex -> new HexDTO(hex.getId(), hex.getType(), hex.getNumberToken()))
                .collect(Collectors.toList());
    }
}
