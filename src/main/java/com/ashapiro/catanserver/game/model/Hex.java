package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.enums.EdgeDirection;
import com.ashapiro.catanserver.game.enums.HexType;
import com.ashapiro.catanserver.game.enums.VertexDirection;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Getter @Setter
public class Hex {

    private Integer id;

    private HexType type;

    private Integer numberToken;

    private Map<EdgeDirection, Hex> neighborHex;

    private Map<VertexDirection, Vertex> vertices;

    private Map<EdgeDirection, Edge> edges;

    public Hex() {
        neighborHex = new EnumMap<>(EdgeDirection.class);
        for (EdgeDirection direction : EdgeDirection.values()) {
            neighborHex.put(direction, null);
        }

        vertices = new EnumMap<>(VertexDirection.class);
        for (VertexDirection direction : VertexDirection.values()) {
            vertices.put(direction, null);
        }

        edges = new EnumMap<>(EdgeDirection.class);
        for (EdgeDirection direction : EdgeDirection.values()) {
            edges.put(direction, null);
        }
    }

    public void setNeighborHex(EdgeDirection edgeDirection, Hex neighbor) {
        if (neighbor != null) {
            neighborHex.put(edgeDirection, neighbor);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hex{id=").append(id).append(", type=").append(type).append(", numberToken=").append(numberToken).append(", neighbors={");
        for (Map.Entry<EdgeDirection, Hex> entry : neighborHex.entrySet()) {
            sb.append(entry.getKey()).append(":");
            if (entry.getValue() != null) {
                sb.append(entry.getValue().getId()).append(", ");
            } else {
                sb.append("null, ");

            }
        }
        if (!neighborHex.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}, vertices={");
        for (Map.Entry<VertexDirection, Vertex> entry : vertices.entrySet()) {
            sb.append(entry.getKey()).append(":");
            if (entry.getValue() != null) {
                sb.append(entry.getValue().getId()).append(", ");
            } else {
                sb.append("null, ");
            }
        }
        if (!vertices.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }

        sb.append("}, edges={");
        for (Map.Entry<EdgeDirection, Edge> entry : edges.entrySet()) {
            sb.append(entry.getKey()).append(":");
            if (entry.getValue() != null) {
                sb.append(entry.getValue().getId()).append(", ");
            } else {
                sb.append("null, ");
            }
        }
        if (!vertices.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}}");
        return sb.toString();
    }
}
