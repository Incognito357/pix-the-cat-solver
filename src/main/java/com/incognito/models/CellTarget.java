package com.incognito.models;

import java.awt.Point;

public class CellTarget {
    private String level;
    private Point target;

    public CellTarget(String level, Point target) {
        this.level = level;
        this.target = target;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Point getTarget() {
        return target;
    }

    public void setTarget(Point target) {
        this.target = target;
    }
}
