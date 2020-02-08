package com.incognito.pix.the.cat.solver.models.enums;

public enum CellType {
    EMPTY,
    WALL,
    EGG,
    TARGET,
    PLAYER_START,
    SPIKY_BOI,
    PORTAL(true),
    CURVE(true);

    private boolean rotateable;

    CellType() {
        rotateable = false;
    }

    CellType(boolean rotateable) {
        this.rotateable = rotateable;
    }

    public boolean isRotateable() {
        return rotateable;
    }
}
