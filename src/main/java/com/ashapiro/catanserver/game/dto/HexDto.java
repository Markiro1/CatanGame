package com.ashapiro.catanserver.game.dto;

import com.ashapiro.catanserver.game.enums.HexType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class HexDto {
    private Integer id;
    private HexType hexType;
    private Integer numberToken;
}
