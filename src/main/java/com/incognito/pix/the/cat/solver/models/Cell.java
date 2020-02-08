package com.incognito.pix.the.cat.solver.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.models.enums.Direction;

public class Cell {
    private CellType type;
    private Direction direction;

    public Cell(CellType type) {
        this.type = type;
        direction = type.isRotateable() ? Direction.UP : Direction.NONE;
    }

    @JsonCreator
    public Cell(@JsonProperty(value = "type") CellType type, @JsonProperty(value = "direction") Direction direction) {
        this.type = type;
        this.direction = direction;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
