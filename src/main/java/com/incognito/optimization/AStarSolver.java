package com.incognito.optimization;

import com.incognito.models.Cell;
import com.incognito.models.enums.CellType;
import com.incognito.models.enums.Direction;
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
    private final Grid<Cell> grid;
    private final Map<Point, Integer> cells = new HashMap<>();
    private final List<Point> path = new LinkedList<>();
    private final Set<Point> open = new HashSet<>();

    public AStarSolver(Grid<Cell> grid, Point start) {
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
        Cell toV = grid.getValue(to);
        if (toV.getType() == CellType.WALL || toV.getType() == CellType.SPIKY_BOI) {
            return true;
        }
        if (toV.getType() == CellType.CURVE) {
            return toV.getDirection() == dir || toV.getDirection() == dir.next();
        }
        if (toV.getType() == CellType.PORTAL) {
            return toV.getDirection() != dir.next().next();
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
