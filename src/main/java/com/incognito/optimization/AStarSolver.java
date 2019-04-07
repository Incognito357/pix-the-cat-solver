package com.incognito.optimization;

import com.incognito.models.CellType;
import com.incognito.models.Direction;
import com.incognito.models.Grid;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class AStarSolver {
    private final Grid<CellType> grid;
    private final Map<Point, Integer> cells = new HashMap<>();
    private final List<Point> path = new LinkedList<>();
    private final Set<Point> open = new HashSet<>();

    public AStarSolver(Grid<CellType> grid, Point start) {
        this.grid = grid;
        open.add(start);
        cells.put(start, 0);
    }

    private Point getNeighbor(Point p, Direction dir){
        switch(dir){
            case UP: return new Point(p.x, p.y - 1);
            case RIGHT: return new Point(p.x + 1, p.y);
            case DOWN: return new Point(p.x, p.y + 1);
            case LEFT: return new Point(p.x - 1, p.y);
        }
        return null;
    }

    private boolean isBlocked(Point from, Point to) {
        Direction dir = grid.getDirection(from, to);
        CellType toV = grid.getValue(to);
        if (toV.equals(CellType.WALL) || toV.equals(CellType.SPIKEY_BOI)) {
            return true;
        }
        if (toV.isCurve()) {
            switch (dir) {
                case UP:
                    return toV.equals(CellType.CURVE_TR) || toV.equals(CellType.CURVE_TL);
                case RIGHT:
                    return toV.equals(CellType.CURVE_TR) || toV.equals(CellType.CURVE_BR);
                case DOWN:
                    return toV.equals(CellType.CURVE_BR) || toV.equals(CellType.CURVE_BL);
                case LEFT:
                    return toV.equals(CellType.CURVE_TL) || toV.equals(CellType.CURVE_BL);
            }
        }
        if (toV.isPortal()) {
            switch (dir) {
                case UP: return !toV.equals(CellType.PORTAL_DOWN);
                case RIGHT: return !toV.equals(CellType.PORTAL_LEFT);
                case DOWN: return !toV.equals(CellType.PORTAL_UP);
                case LEFT: return !toV.equals(CellType.PORTAL_RIGHT);
            }
        }
        return false;
    }

    public void Solve(){
        while (!open.isEmpty()){
            Point cur = open.stream().findFirst().get();
            int weight = cells.get(cur);
            open.remove(cur);
            for (Direction dir : Direction.values()){
                Point neighbor = getNeighbor(cur, dir);
                Stream<Point> uniquePathCells = path.stream().distinct();
                if (!isBlocked(cur, neighbor)) {
                    int cost = weight + 1;

                    if (cost < weight) {
                        open.remove(neighbor);
                    }

                    if (!open.contains(neighbor)){
                        cells.put(neighbor, cost);
                        open.add(neighbor);
                    }
                }
            }
        }
    }
}
