package com.ashapiro.catanserver.game.enums;

public enum HexType {
    PASTURE,
    FIELD,
    FOREST,
    HILL,
    MOUNTAIN,
    DESERT;

    public Resource getResource() {
        switch (this) {
            case PASTURE: return Resource.WOOL;
            case FIELD: return Resource.GRAIN;
            case FOREST: return Resource.LUMBER;
            case HILL: return Resource.BRICK;
            case MOUNTAIN: return Resource.ORE;
            case DESERT: return null;
            default: throw new IllegalArgumentException("Unknown hex type: " + this);
        }
    }
}
