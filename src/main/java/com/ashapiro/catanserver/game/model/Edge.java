package com.ashapiro.catanserver.game.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter @Setter
public class Edge {
    private Integer id;

    private List<Vertex> vertices = new ArrayList<>();
}
