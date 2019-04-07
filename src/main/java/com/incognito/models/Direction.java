package com.incognito.models;

public enum Direction {
    UP, RIGHT, DOWN, LEFT;
    public Direction next(){
        return Direction.values()[(this.ordinal() + 1) % 4];
    }
}
