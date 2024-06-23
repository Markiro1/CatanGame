package com.ashapiro.catanserver.game;

import com.ashapiro.catanserver.game.enums.EdgeDirection;
import com.ashapiro.catanserver.game.enums.HarborType;
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
import java.util.stream.Collectors;

public class MapGenerator {

    private VertexFactory vertexFactory;

    private HexFactory hexFactory;

    private EdgeFactory edgeFactory;

    private Map<Coordinates, Hex> gameMap;

    private int seed;

    private Random mapRandom;

    public MapGenerator() {
        this.vertexFactory = new VertexFactory();
        this.hexFactory = new HexFactory();
        this.edgeFactory = new EdgeFactory();
        this.gameMap = new HashMap<>();
        this.seed = new Random().nextInt();
        this.mapRandom = new Random(seed);
    }

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
                        currentHex.setNeighborHex(EdgeDirection.TL, gameMap.getOrDefault(new Coordinates(x, y - 1), null));
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
                    Vertex vertex = vertexFactory.createVertex(currentHex);
                    currentHex.getVertices().put(VertexDirection.N, vertex);

                    if (hexTL != null) {
                        hexTL.getVertices().put(VertexDirection.SE, vertex);
                        vertex.getHexes().add(hexTL);
                    }

                    if (hexTR != null) {
                        hexTR.getVertices().put(VertexDirection.SW, vertex);
                        vertex.getHexes().add(hexTR);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.NE) && currentHex.getVertices().get(VertexDirection.NE) == null) {
                    Vertex vertex = vertexFactory.createVertex(currentHex);
                    currentHex.getVertices().put(VertexDirection.NE, vertex);

                    if (hexTR != null) {
                        hexTR.getVertices().put(VertexDirection.S, vertex);
                        vertex.getHexes().add(hexTR);
                    }

                    if (hexR != null) {
                        hexR.getVertices().put(VertexDirection.NW, vertex);
                        vertex.getHexes().add(hexR);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.SE) && currentHex.getVertices().get(VertexDirection.SE) == null) {
                    Vertex vertex = vertexFactory.createVertex(currentHex);
                    currentHex.getVertices().put(VertexDirection.SE, vertex);

                    if (hexR != null) {
                        hexR.getVertices().put(VertexDirection.SW, vertex);
                        vertex.getHexes().add(hexR);
                    }

                    if (hexDR != null) {
                        hexDR.getVertices().put(VertexDirection.N, vertex);
                        vertex.getHexes().add(hexDR);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.S) && currentHex.getVertices().get(VertexDirection.S) == null) {
                    Vertex vertex = vertexFactory.createVertex(currentHex);
                    currentHex.getVertices().put(VertexDirection.S, vertex);

                    if (hexDR != null) {
                        hexDR.getVertices().put(VertexDirection.NW, vertex);
                        vertex.getHexes().add(hexDR);
                    }

                    if (hexDL != null) {
                        hexDL.getVertices().put(VertexDirection.NE, vertex);
                        vertex.getHexes().add(hexDL);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.SW) && currentHex.getVertices().get(VertexDirection.SW) == null) {
                    Vertex vertex = vertexFactory.createVertex(currentHex);
                    currentHex.getVertices().put(VertexDirection.SW, vertex);

                    if (hexDL != null) {
                        hexDL.getVertices().put(VertexDirection.N, vertex);
                        vertex.getHexes().add(hexDL);
                    }

                    if (hexL != null) {
                        hexL.getVertices().put(VertexDirection.SE, vertex);
                        vertex.getHexes().add(hexL);
                    }
                }

                if (currentHex.getVertices().containsKey(VertexDirection.NW) && currentHex.getVertices().get(VertexDirection.NW) == null) {
                    Vertex vertex = vertexFactory.createVertex(currentHex);
                    currentHex.getVertices().put(VertexDirection.NW, vertex);

                    if (hexL != null) {
                        hexL.getVertices().put(VertexDirection.NE, vertex);
                        vertex.getHexes().add(hexL);
                    }

                    if (hexTL != null) {
                        hexTL.getVertices().put(VertexDirection.S, vertex);
                        vertex.getHexes().add(hexTL);
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
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.N), currentHex.getVertices().get(VertexDirection.NE), currentHex);
                    currentHex.getEdges().put(EdgeDirection.TR, edge);

                    if (hexTR != null) {
                        hexTR.getEdges().put(EdgeDirection.DL, edge);
                        edge.getHexes().add(hexTR);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.R) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.NE), currentHex.getVertices().get(VertexDirection.SE), currentHex);
                    currentHex.getEdges().put(EdgeDirection.R, edge);

                    if (hexR != null) {
                        hexR.getEdges().put(EdgeDirection.L, edge);
                        edge.getHexes().add(hexR);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.DR) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.SE), currentHex.getVertices().get(VertexDirection.S), currentHex);
                    currentHex.getEdges().put(EdgeDirection.DR, edge);

                    if (hexDR != null) {
                        hexDR.getEdges().put(EdgeDirection.TL, edge);
                        edge.getHexes().add(hexDL);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.DL) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.S), currentHex.getVertices().get(VertexDirection.SW), currentHex);
                    currentHex.getEdges().put(EdgeDirection.DL, edge);

                    if (hexDL != null) {
                        hexDL.getEdges().put(EdgeDirection.TR, edge);
                        edge.getHexes().add(hexDL);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.L) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.SW), currentHex.getVertices().get(VertexDirection.NW), currentHex);
                    currentHex.getEdges().put(EdgeDirection.L, edge);

                    if (hexL != null) {
                        hexL.getEdges().put(EdgeDirection.R, edge);
                        edge.getHexes().add(hexL);
                    }
                }

                if (currentHex.getEdges().get(EdgeDirection.TL) == null) {
                    Edge edge = edgeFactory.createEdge(currentHex.getVertices().get(VertexDirection.NW), currentHex.getVertices().get(VertexDirection.N), currentHex);
                    currentHex.getEdges().put(EdgeDirection.TL, edge);

                    if (hexTL != null) {
                        hexTL.getEdges().put(EdgeDirection.DR, edge);
                        edge.getHexes().add(hexTL);
                    }
                }
            }
        }

        int numOfHexes = numInRows.stream()
                .mapToInt(Integer::intValue)
                .sum();

        List<HexType> hexTypes = createHexTypes(numOfHexes);
        shuffle(hexTypes);
        addDesert(hexTypes, numOfHexes);

        List<Hex> hexes = hexFactory.getHexes();
        setHexesType(hexes, hexTypes);
        generateHexesNumberTokens(hexes);
        generateHarbors();

        return gameMap;
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


    public int getSeed() {
        return seed;
    }

    private void setHexesType(List<Hex> hexes, List<HexType> hexTypes) {
        for (int i = 0; i < hexes.size(); i++) {
            hexes.get(i).setType(hexTypes.get(i));
        }
    }

    private List<HexType> createHexTypes(int numOfHexes) {
        numOfHexes--;
        int coefficientNum = numOfHexes / 3;
        int numbOfPrimaryResources = coefficientNum * 2;
        int numbOfSecondaryResources = coefficientNum;
        List<HexType> hexTypes = new ArrayList<>();
        for (int i = 0; i < numbOfPrimaryResources; i++) {
            hexTypes.add(HexType.values()[i % 3]);
        }

        for (int i = 0; i < numbOfSecondaryResources; i++) {
            hexTypes.add(HexType.values()[3 + (i % 2)]);
        }
        return hexTypes;
    }

    private <T> void shuffle(List<T> list) {
        for (int i = 0; i < list.size(); i++) {
            int randomIndex = mapRandom.nextInt(list.size());
            Collections.swap(list, i, randomIndex);
        }
    }

    private void addDesert(List<HexType> hexTypes, int numOfHexes) {
        hexTypes.add(HexType.DESERT);
        Collections.swap(hexTypes, numOfHexes / 2, numOfHexes - 1);
    }

    private void generateHexesNumberTokens(List<Hex> hexes) {
        List<Integer> numberTokens = new ArrayList<>(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        Queue<Integer> numberTokensQueue = new ArrayDeque<>();

        while (numberTokensQueue.size() < hexes.size()) {
            shuffle(numberTokens);
            numberTokensQueue.addAll(numberTokens);
        }

        for (int i = 0; i < hexes.size(); i++) {
            if (hexes.get(i) == hexes.get(hexes.size() / 2)) {
                continue;
            }
            hexes.get(i).setNumberToken(numberTokensQueue.poll());
        }
    }

    private void generateHarbors() {
        List<Edge> boundaryEdges = getEdges().stream()
                .filter(edge -> edge.getHexes().size() == 1)
                .collect(Collectors.toList());
        List<Edge> boundaryEdgesInOrder = getBoundaryEdgesInRightOrder(boundaryEdges);

        List<Integer> spaceBetweenHarbors = new ArrayList<>(List.of(3, 3, 4));
        List<HarborType> harborTypes = new ArrayList<>(List.of(
                HarborType.ORE,
                HarborType.BRICK,
                HarborType.LUMBER,
                HarborType.GRAIN,
                HarborType.WOOL,
                HarborType.GENERIC,
                HarborType.GENERIC,
                HarborType.GENERIC,
                HarborType.GENERIC
        ));
        shuffle(harborTypes);
        int startIndexEdge = mapRandom.nextInt(boundaryEdgesInOrder.size());
        int finalIndexEdge = startIndexEdge + boundaryEdges.size();
        int index = startIndexEdge;

        int i = 0;
        while (index < finalIndexEdge) {
            List<Vertex> vertices = boundaryEdgesInOrder.get(index % boundaryEdgesInOrder.size()).getNeighborVertices();
            HarborType harborType = harborTypes.get(i % harborTypes.size());
            index += spaceBetweenHarbors.get(i % spaceBetweenHarbors.size());
            i++;

            for (Vertex vertex : vertices) {
                vertex.setHarborType(harborType);
            }
        }
    }

    private List<Edge> getBoundaryEdgesInRightOrder(List<Edge> boundaryEdges) {
        List<Edge> boundaryEdgesInOrder = new ArrayList<>();
        Edge currentEdge = boundaryEdges.get(0);
        boundaryEdgesInOrder.add(currentEdge);
        boolean isFound = true;

        while (isFound) {
            isFound = false;
            currentEdge = MapUtil.getNeighboursEdgeToEdge(currentEdge).stream()
                    .filter(edge -> boundaryEdges.contains(edge) && !boundaryEdgesInOrder.contains(edge))
                    .findFirst()
                    .orElse(null);

            if (currentEdge != null) {
                isFound = true;
                boundaryEdgesInOrder.add(currentEdge);
            }
        }

        return new ArrayList<>(boundaryEdgesInOrder);
    }
}
