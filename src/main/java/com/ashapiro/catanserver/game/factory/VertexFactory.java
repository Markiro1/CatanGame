package com.ashapiro.catanserver.game.factory;

import com.ashapiro.catanserver.game.enums.VertexBuildingType;
import com.ashapiro.catanserver.game.model.Hex;
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

    public Vertex createVertex(Hex hex) {
        Vertex vertex = new Vertex();
        vertices.add(vertex);
        vertex.getHexes().add(hex);
        vertex.setId(id++);
        vertex.setType(VertexBuildingType.NONE);
        return vertex;
    }
}
