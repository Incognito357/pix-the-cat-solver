package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.enums.Direction;
import java.awt.Point;

public class StartLocation implements Standstill {
    private Point location;
    private Direction direction;
    private Visit nextVisit;

    public StartLocation() {}

    public StartLocation(Point location, Direction direction) {
        this.location = location;
        this.direction = direction;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public StartLocation getStartLocation() {
        return this;
    }

    @Override
    public Visit getNextVisit() {
        return nextVisit;
    }

    @Override
    public void setNextVisit(Visit nextVisit) {
        this.nextVisit = nextVisit;
    }
}
