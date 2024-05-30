package com.ashapiro.catanserver.game.factory;

import com.ashapiro.catanserver.game.model.Edge;
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

    public Edge createEdge(Vertex vertex1, Vertex vertex2) {
        Edge edge = new Edge();
        edge.getVertices().add(vertex1);
        edge.getVertices().add(vertex2);

        vertex1.getEdges().add(edge);
        vertex2.getEdges().add(edge);

        edge.setId(id++);
        edges.add(edge);
        return edge;
    }
}
