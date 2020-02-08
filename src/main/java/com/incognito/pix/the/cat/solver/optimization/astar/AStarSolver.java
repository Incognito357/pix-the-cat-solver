package com.incognito.pix.the.cat.solver.optimization.astar;

import com.incognito.pix.the.cat.solver.models.Cell;
import com.incognito.pix.the.cat.solver.models.Grid;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.models.enums.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AStarSolver {
    private final Grid<Cell> grid;
    private final TreeSet<AStarNode> open = new TreeSet<>(Comparator.comparing(AStarNode::getF));
    private final Set<AStarNode> closed = new HashSet<>();
    private final Point target;
    private final Point start;
    private final Map<Point, Integer> trail;
    private final boolean avoidTargets;

    public AStarSolver(Grid<Cell> grid, Map<Point, Integer> trail, Point start, Point target, boolean avoidTargets) {
        this.grid = grid;
        this.target = target;
        this.trail = trail;
        this.start = start;
        this.avoidTargets = avoidTargets;
        open.add(new AStarNode(null, start, 0, 0));
    }

    private Point getNeighbor(Point p, Direction dir){
        switch(dir){
            case UP: return new Point(p.x, p.y - 1);
            case RIGHT: return new Point(p.x + 1, p.y);
            case DOWN: return new Point(p.x, p.y + 1);
            case LEFT: return new Point(p.x - 1, p.y);
        }
        return p;
    }

    private int getEuclidDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private boolean isBlocked(AStarNode from, Point to) {
        Direction dir = grid.getDirection(from.getP(), to);
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
        if (toV.getType() == CellType.TARGET) {
            return avoidTargets;
        }
        if (trail.containsKey(to)) {
            return from.getG() <= trail.get(to);
        }
        return false;
    }

    public List<Point> Solve(){
        while (!open.isEmpty()){
            AStarNode p = open.pollFirst();
            closed.add(p);

            assert p != null;
            if (target.equals(p.getP())) {
                //path found
                List<Point> path = new ArrayList<>();
                AStarNode n = p;
                Point np;
                while (!(np = n.getP()).equals(start)) {
                    path.add(0, np);
                    n = n.getParent();
                }
                return path;
            }

            for (Direction dir : Direction.values()) {
                Point nextPoint = getNeighbor(p.getP(), dir);
                AStarNode next = new AStarNode(p, nextPoint, p.getG() + 1, getEuclidDistance(nextPoint, target));
                if (open.contains(next) || closed.contains(next) || isBlocked(p, nextPoint)) {
                    continue;
                }
                open.add(next);
            }
        }
        //no path
        return Collections.emptyList();
    }
}
