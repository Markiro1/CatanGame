package com.ashapiro.catanserver.game.model;

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

    private List<Edge> edges = new ArrayList<>();
}
