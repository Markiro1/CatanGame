package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.game.enums.EdgeDirection;
import com.ashapiro.catanserver.game.enums.HexType;
import com.ashapiro.catanserver.game.enums.VertexDirection;
import com.ashapiro.catanserver.game.factory.EdgeFactory;
import com.ashapiro.catanserver.game.factory.HexFactory;
import com.ashapiro.catanserver.game.factory.VertexFactory;
import com.ashapiro.catanserver.game.model.Coordinates;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.Hex;
import com.ashapiro.catanserver.game.model.Vertex;

import java.util.*;

public class MapGenerator {

    private VertexFactory vertexFactory = new VertexFactory();

    private HexFactory hexFactory = new HexFactory();

    private EdgeFactory edgeFactory = new EdgeFactory();

    private Map<Coordinates, Hex> gameMap = new HashMap<>();

    public Map<Coordinates, Hex> generateMap(List<Integer> numInRows) {

        for (int y = 0; y < numInRows.size(); y++) {
            for (int x = 0; x < numInRows.get(y); x++) {
                Coordinates coordinates = new Coordinates(x, y);
                Hex hex = hexFactory.createHex();
                gameMap.put(coordinates, hex);
            }
        }

        for (int y = 0; y < numInRows.size(); y++) {
            for (int x = 0; x < numInRows.get(y); x++) {
                Coordinates coordinates = new Coordinates(x, y);
                Hex currentHex = gameMap.getOrDefault(coordinates, null);

                if (currentHex == null) continue;

                if (y - 1 >= 0) {
                    if (numInRows.get(y) > numInRows.get(y - 1)) {
                        currentHex.setNeighborHex(EdgeDirection.TL, gameMap.getOrDefault(new Coordinates(x - 1, y - 1), null));
                        currentHex.setNeighborHex(EdgeDirection.TR, gameMap.getOrDefault(new Coordinates(x, y - 1), null));
                    } else {
                        currentHex.setNeighborHex(EdgeDirection.L, gameMap.getOrDefault(new Coordinates(x, y - 1), null));
                        currentHex.setNeighborHex(EdgeDirection.TR, gameMap.getOrDefault(new Coordinates(x + 1, y - 1), null));
                    }
                }

                currentHex.setNeighborHex(EdgeDirection.L, gameMap.getOrDefault(new Coordinates(x - 1, y), null));
                currentHex.setNeighborHex(EdgeDirection.R, gameMap.getOrDefault(new Coordinates(x + 1, y), null));

                if (y + 1 < numInRows.size()) {
                    if (numInRows.get(y) > numInRows.get(y + 1)) {
                        currentHex.setNeighborHex(EdgeDirection.DL, gameMap.getOrDefault(new Coordinates(x - 1, y + 1), null));
                        currentHex.setNeighborHex(EdgeDirection.DR, gameMap.getOrDefault(new Coordinates(x, y + 1), null));
                    } else {
                        currentHex.setNeighborHex(EdgeDirection.DL, gameMap.getOrDefault(new Coordinates(x, y + 1), null));
                        currentHex.setNeighborHex(EdgeDirection.DR, gameMap.getOrDefault(new Coordinates(x + 1, y + 1), null));
                    }
                }
            }
        }


        for (int y = 0; y < numInRows.size(); y++) {
            for (int x = 0; x < numInRows.get(y); x++) {
                Coordinates coordinates = new Coordinates(x, y);
                Hex currentHex = gameMap.getOrDefault(coordinates, null);

                Hex hexTR = currentHex.getNeighborHex().get(EdgeDirection.TR);
                Hex hexR = currentHex.getNeighborHex().get(EdgeDirection.R);
                Hex hexDR = currentHex.getNeighborHex().get(EdgeDirection.DR);
                Hex hexDL = currentHex.getNeighborHex().get(EdgeDirection.DL);
                Hex hexL = currentHex.getNeighborHex().get(EdgeDirection.L);
                Hex hexTL = currentHex.getNeighborHex().get(EdgeDirection.TL);

                if (currentHex.getVertices().containsKey(VertexDirection.N) && currentHex.getVertices().get(VertexDirection.N) == null) {
                    Vertex vertex = vertexFactory.createVertex();
                    currentHex.getVertices().put(VertexDirection.N, vertex);

                    if (hexTL != null) {
                        hexTL.getVertices().put(VertexDirection.SE, vertex);
                    }

                    if (hexTR != null) {
                        hexTR.getVertices().put(VertexDirection.SW, vertex);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.NE) && currentHex.getVertices().get(VertexDirection.NE) == null) {
                    Vertex vertex = vertexFactory.createVertex();
                    currentHex.getVertices().put(VertexDirection.NE, vertex);

                    if (hexTR != null) {
                        hexTR.getVertices().put(VertexDirection.S, vertex);
                    }

                    if (hexR != null) {
                        hexR.getVertices().put(VertexDirection.NW, vertex);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.SE) && currentHex.getVertices().get(VertexDirection.SE) == null) {
                    Vertex vertex = vertexFactory.createVertex();
                    currentHex.getVertices().put(VertexDirection.SE, vertex);

                    if (hexR != null) {
                        hexR.getVertices().put(VertexDirection.SW, vertex);
                    }

                    if (hexDR != null) {
                        hexDR.getVertices().put(VertexDirection.N, vertex);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.S) && currentHex.getVertices().get(VertexDirection.S) == null) {
                    Vertex vertex = vertexFactory.createVertex();
                    currentHex.getVertices().put(VertexDirection.S, vertex);

                    if (hexDR != null) {
                        hexDR.getVertices().put(VertexDirection.NW, vertex);
                    }

                    if (hexDL != null) {
                        hexDL.getVertices().put(VertexDirection.NE, vertex);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.SW) && currentHex.getVertices().get(VertexDirection.SW) == null) {
                    Vertex vertex = vertexFactory.createVertex();
                    currentHex.getVertices().put(VertexDirection.SW, vertex);

                    if (hexDL != null) {
                        hexDL.getVertices().put(VertexDirection.N, vertex);
                    }

                    if (hexL != null) {
                        hexL.getVertices().put(VertexDirection.SE, vertex);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.NW) && currentHex.getVertices().get(VertexDirection.NW) == null) {
                    Vertex vertex = vertexFactory.createVertex();
                    currentHex.getVertices().put(VertexDirection.NW, vertex);

                    if (hexL != null) {
                        hexL.getVertices().put(VertexDirection.NE, vertex);
                    }

                    if (hexTL != null) {
                        hexTL.getVertices().put(VertexDirection.S, vertex);
                    }
                }
            }
        }

        for (int y = 0; y < numInRows.size(); y++) {
            for (int x = 0; x < numInRows.get(y); x++) {
                Coordinates coordinates = new Coordinates(x, y);
                Hex currentHex = gameMap.getOrDefault(coordinates, null);

                Hex hexTR = currentHex.getNeighborHex().get(EdgeDirection.TR);
                Hex hexR = currentHex.getNeighborHex().get(EdgeDirection.R);
                Hex hexDR = currentHex.getNeighborHex().get(EdgeDirection.DR);
                Hex hexDL = currentHex.getNeighborHex().get(EdgeDirection.DL);
                Hex hexL = currentHex.getNeighborHex().get(EdgeDirection.L);
                Hex hexTL = currentHex.getNeighborHex().get(EdgeDirection.TL);

                if (currentHex.getEdges().get(EdgeDirection.TR) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.N), currentHex.getVertices().get(VertexDirection.NE));
                    currentHex.getEdges().put(EdgeDirection.TR, edge);

                    if (hexTR != null) {
                        hexTR.getEdges().put(EdgeDirection.DL, edge);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.R) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.NE), currentHex.getVertices().get(VertexDirection.SE));
                    currentHex.getEdges().put(EdgeDirection.R, edge);

                    if (hexR != null) {
                        hexR.getEdges().put(EdgeDirection.L, edge);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.DR) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.SE), currentHex.getVertices().get(VertexDirection.S));
                    currentHex.getEdges().put(EdgeDirection.DR, edge);

                    if (hexDR != null) {
                        hexDR.getEdges().put(EdgeDirection.TL, edge);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.DL) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.S), currentHex.getVertices().get(VertexDirection.SW));
                    currentHex.getEdges().put(EdgeDirection.DL, edge);

                    if (hexDL != null) {
                        hexDL.getEdges().put(EdgeDirection.TR, edge);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.L) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.SW), currentHex.getVertices().get(VertexDirection.NW));
                    currentHex.getEdges().put(EdgeDirection.L, edge);

                    if (hexL != null) {
                        hexL.getEdges().put(EdgeDirection.R, edge);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.TL) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.NW), currentHex.getVertices().get(VertexDirection.N));
                    currentHex.getEdges().put(EdgeDirection.TL, edge);

                    if (hexTL != null) {
                        hexTL.getEdges().put(EdgeDirection.DR, edge);
                    }
                }
            }
        }

        int numOfHexes = numInRows.stream()
                .mapToInt(Integer::intValue)
                .sum();
        numOfHexes--;
        int koefNum = numOfHexes / 3;

        List<HexType> hexTypes = new ArrayList<>();

        for (int i = 0; i < koefNum * 2; i++) {
            hexTypes.add(HexType.values()[i % 3]);
        }

        for (int i = 0; i < koefNum; i++) {
            hexTypes.add(HexType.values()[3 + (i % 2)]);
        }

        Collections.shuffle(hexTypes);
        hexTypes.add(HexType.DESERT);
        Collections.swap(hexTypes, numOfHexes / 2, numOfHexes);


        List<Hex> hexes = hexFactory.getHexes();
        for (int i = 0; i < hexes.size(); i++) {
            hexes.get(i).setType(hexTypes.get(i));
        }

        List<Integer> numberTokens = new ArrayList<>(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        Queue<Integer> numberTokensQueue = new ArrayDeque<>();

        while (numberTokensQueue.size() < hexes.size()) {
            Collections.shuffle(numberTokens);
            numberTokensQueue.addAll(numberTokens);
        }

        for (int i = 0; i < hexes.size(); i++) {
            if (hexes.get(i) == hexes.get(hexes.size() / 2)) {
                continue;
            }
            hexes.get(i).setNumberToken(numberTokensQueue.poll());
        }

/*
        gameMap.values().stream()
                .sorted(Comparator.comparing(Hex::getId))
                .forEach(System.out::println);*/

        return gameMap;
    }

    public List<Integer> getNeighborVertexById(Integer id) {
        List<Vertex> vertices = vertexFactory.getVertices();
        Vertex currentVertex = vertices.stream()
                .filter(vertex -> vertex.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Vertex not found with id: " + id));

        List<Integer> neighborVertexIds = currentVertex.getEdges().stream()
                .flatMap(edge -> edge.getVertices().stream())
                .filter(vertex -> !vertex.getId().equals(currentVertex.getId()))
                .map(Vertex::getId)
                .toList();

        return neighborVertexIds;
    }

    public List<Hex> getHexes() {
        return hexFactory.getHexes();
    }

    public List<Vertex> getVertices() {
        return vertexFactory.getVertices();
    }

    public List<Edge> getEdges() {
        return edgeFactory.getEdges();
    }
}
