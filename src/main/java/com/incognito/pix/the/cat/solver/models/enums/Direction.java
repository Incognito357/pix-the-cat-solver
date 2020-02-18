package com.incognito.pix.the.cat.solver.models.enums;

import org.apache.commons.math3.util.FastMath;

public enum Direction {
    UP, RIGHT, DOWN, LEFT, NONE;

    public Direction next(){
        if (this == NONE) {
            return NONE;
        }
        return Direction.values()[(this.ordinal() + 1) % 4];
    }

    public Direction prev() {
        if (this == NONE) {
            return NONE;
        }
        return Direction.values()[FastMath.floorMod(this.ordinal() - 1, 4)];
    }

    private static final Direction[] CARDINAL = { UP, RIGHT, DOWN, LEFT };
    public static Direction[] cardinals() {
        return CARDINAL;
    }
}
