package com.incognito.models;

public enum CellType {
    EMPTY,
    WALL,
    EGG,
    TARGET,
    PLAYER_START,
    SPIKEY_BOI,
    PORTAL_UP,
    PORTAL_RIGHT,
    PORTAL_DOWN,
    PORTAL_LEFT,
    CURVE_TR,
    CURVE_BR,
    CURVE_BL,
    CURVE_TL;

    public CellType next() {
        switch (this) {
            case PORTAL_UP: return PORTAL_RIGHT;
            case PORTAL_RIGHT: return PORTAL_DOWN;
            case PORTAL_DOWN: return PORTAL_LEFT;
            case PORTAL_LEFT: return PORTAL_UP;
            case CURVE_TR: return CURVE_BR;
            case CURVE_BR: return CURVE_BL;
            case CURVE_BL: return CURVE_TL;
            case CURVE_TL: return CURVE_TR;
            default: return this;
        }
    }

    public Direction getDirection() {
        switch (this) {
            case PORTAL_RIGHT:
            case CURVE_BR:
                return Direction.RIGHT;
            case PORTAL_DOWN:
            case CURVE_BL:
                return Direction.DOWN;
            case PORTAL_LEFT:
            case CURVE_TL:
                return Direction.LEFT;
            case PORTAL_UP:
            case CURVE_TR:
            default: return Direction.UP;
        }
    }

    public boolean isPortal() {
        switch (this) {
            case PORTAL_UP:
            case PORTAL_RIGHT:
            case PORTAL_DOWN:
            case PORTAL_LEFT:
                return true;
            default:
                return false;
        }
    }

    public boolean isCurve() {
        switch (this) {
            case CURVE_TR:
            case CURVE_BR:
            case CURVE_BL:
            case CURVE_TL:
                return true;
            default: return false;
        }
    }

    public boolean isRotateable() {
        switch (this) {
            case PORTAL_UP:
            case PORTAL_RIGHT:
            case PORTAL_DOWN:
            case PORTAL_LEFT:
            case CURVE_TR:
            case CURVE_BR:
            case CURVE_BL:
            case CURVE_TL:
                return true;
            default:
                return false;
        }
    }
}
