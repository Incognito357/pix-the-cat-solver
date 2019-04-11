package com.incognito.models.enums;

public enum Direction {
    UP, RIGHT, DOWN, LEFT, NONE;

    public Direction next(){
        if (this == NONE) {
            return NONE;
        }
        return Direction.values()[(this.ordinal() + 1) % 4];
    }
}
