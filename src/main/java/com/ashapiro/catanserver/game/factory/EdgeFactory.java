package com.ashapiro.catanserver.game.factory;

import com.ashapiro.catanserver.game.enums.EdgeBuildingType;
import com.ashapiro.catanserver.game.model.Edge;
import com.ashapiro.catanserver.game.model.Hex;
import com.ashapiro.catanserver.game.model.Vertex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter @Setter
public class EdgeFactory {

    private Integer id = 0;

    private List<Edge> edges = new ArrayList<>();

    public Edge createEdge(Vertex vertex1, Vertex vertex2, Hex hex) {
        Edge edge = new Edge();
        edge.getNeighborVertices().add(vertex1);
        edge.getNeighborVertices().add(vertex2);
        edge.getHexes().add(hex);

        vertex1.getNeighbourEdges().add(edge);
        vertex2.getNeighbourEdges().add(edge);

        edge.setId(id++);
        edges.add(edge);
        edge.setType(EdgeBuildingType.NONE);
        return edge;
    }
}
