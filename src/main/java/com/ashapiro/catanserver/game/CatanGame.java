package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.enums.GameState;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.Hex;
import com.ashapiro.catanserver.game.model.Player;
import com.ashapiro.catanserver.game.model.Vertex;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.List;

@Getter @Setter
public class CatanGame {

    private GameState gameState;

    private List<Player> players;

    private List<Hex> hexes;

    private List<Edge> edges;

    private List<Vertex> vertices;

    public CatanGame(List<Player> players, List<Hex> hexes, List<Edge> edges, List<Vertex> vertices) {
        this.players = players;
        this.hexes = hexes;
        this.edges = edges;
        this.vertices = vertices;
        gameState = GameState.WAITING;

        GameManager gameManager = new GameManager();
        new Thread(gameManager).start();
    }

    private class GameManager implements Runnable {
        @Override
        @SneakyThrows
        public void run() {
            // todo: game logic
            Thread.sleep(3000);
            long count = players.stream()
                    .filter(p -> p.getIsReady())
                    .count();
            if (count == players.size()) {
                gameState = GameState.STARTED;
            }
            do {
                for (Player p : players) {

                }
            } while (true ); // todo: add some logic
        }
    }
}
