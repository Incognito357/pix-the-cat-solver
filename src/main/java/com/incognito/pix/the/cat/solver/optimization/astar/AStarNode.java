package com.incognito.pix.the.cat.solver.optimization.astar;

import java.awt.Point;
import java.util.Objects;

public class AStarNode {
    private final AStarNode parent;
    private final Point p;
    private int g;
    private int h;

    public AStarNode(AStarNode parent, Point p, int g, int h) {
        this.parent = parent;
        this.p = p;
        this.g = g;
        this.h = h;
    }

    public AStarNode getParent() {
        return parent;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public Point getP() {
        return p;
    }

    public int getF() {
        return g + h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AStarNode aStarNode = (AStarNode) o;
        return Objects.equals(p, aStarNode.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p);
    }
}
