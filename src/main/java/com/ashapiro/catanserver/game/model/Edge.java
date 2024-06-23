package com.ashapiro.catanserver.game.model;

import com.ashapiro.catanserver.game.enums.EdgeBuildingType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter @Setter
@ToString(exclude = "neighborVertices")
@EqualsAndHashCode
public class Edge {
    private Integer id;

    private List<Hex> hexes = new ArrayList<>();

    private List<Vertex> neighborVertices = new ArrayList<>();

    private EdgeBuildingType type;

    private User user;
}
