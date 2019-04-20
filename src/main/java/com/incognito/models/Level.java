package com.incognito.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class Level {
    private String name;
    private Grid<Cell> grid;
    private Map<Point, Point> parentLinks = new HashMap<>();
    private Map<Point, Point> childLinks = new HashMap<>();

    @JsonIgnore
    private Level parent;

    @JsonIgnore
    private Level child;

    public Level() {}

    public Level(String name, Grid<Cell> grid) {
        this.name = name;
        this.grid = grid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grid<Cell> getGrid() {
        return grid;
    }

    public void setGrid(Grid<Cell> grid) {
        this.grid = grid;
    }


    public Map<Point, Point> getParentLinks() {
        return parentLinks;
    }

    public void setParentLinks(Map<Point, Point> parentLinks) {
        this.parentLinks = parentLinks;
    }

    public Map<Point, Point> getChildLinks() {
        return childLinks;
    }

    public void setChildLinks(Map<Point, Point> childLinks) {
        this.childLinks = childLinks;
    }

    public Level getParent() {
        return parent;
    }

    void setParent(Level parent) {
        this.parent = parent;
    }

    public Level getChild() {
        return child;
    }

    void setChild(Level child) {
        this.child = child;
    }
}
