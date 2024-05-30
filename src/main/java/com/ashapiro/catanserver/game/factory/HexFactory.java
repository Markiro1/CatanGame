package com.ashapiro.catanserver.game.factory;

import com.ashapiro.catanserver.game.model.Hex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class HexFactory {

    private Integer id = 0;

    private List<Hex> hexes = new ArrayList<>();

    public Hex createHex() {
        Hex hex = new Hex();
        hex.setId(id++);
        hexes.add(hex);
        return hex;
    }
}
