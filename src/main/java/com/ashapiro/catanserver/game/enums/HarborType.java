package com.ashapiro.catanserver.game.enums;

public enum HarborType {

    BRICK,
    LUMBER,
    WOOL,
    GRAIN,
    ORE,
    NONE,
    GENERIC;

    public static HarborType getTypeByResource(Resource resource) {
        switch (resource) {
            case BRICK: return BRICK;
            case LUMBER: return LUMBER;
            case WOOL: return WOOL;
            case GRAIN: return GRAIN;
            case ORE: return ORE;
            default: return NONE;
        }
    }
}
