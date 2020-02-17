package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.enums.Direction;
import java.awt.Point;

public class ExitLocation {
    private Point point;
    private Direction direction;

    public ExitLocation() {}

    public ExitLocation(Point point, Direction direction) {
        this.point = point;
        this.direction = direction;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
