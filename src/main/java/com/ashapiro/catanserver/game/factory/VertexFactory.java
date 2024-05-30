package com.ashapiro.catanserver.game.factory;

import com.ashapiro.catanserver.game.model.Vertex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class VertexFactory {

    private Integer id = 0;

    private List<Vertex> vertices = new ArrayList<>();

    public Vertex createVertex() {
        Vertex vertex = new Vertex();
        vertices.add(vertex);
        vertex.setId(id++);
        return vertex;
    }
}
