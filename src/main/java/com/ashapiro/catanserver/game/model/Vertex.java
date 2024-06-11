package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.User;
import com.ashapiro.catanserver.game.enums.VertexBuildingType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Vertex {

    private Integer id;

    private List<Hex> hexes = new ArrayList<>();

    private List<Edge> neighbourEdges = new ArrayList<>();

    private VertexBuildingType type;

    private User user;
}
