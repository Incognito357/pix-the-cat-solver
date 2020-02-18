package com.incognito.pix.the.cat.solver.optimization.astar;

import com.incognito.pix.the.cat.solver.models.Cell;
import com.incognito.pix.the.cat.solver.models.Grid;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.models.enums.Direction;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AStarSolver {
    private final Grid<Cell> grid;
    private final Map<Point, AStarNode> open = new HashMap<>();
    private final Map<Point, AStarNode> closed = new HashMap<>();
    private final Point target;
    private final Point start;
    private final Map<Point, Integer> trail;
    private final Set<Point> collectedEggs;
    private final Set<Point> collectedTargets;
    private final boolean avoidTargets;

    public AStarSolver(Grid<Cell> grid, Point start, Point target, Map<Point, Integer> trail,
                       Set<Point> collectedEggs, Set<Point> collectedTargets, boolean avoidTargets) {
        this.grid = grid;
        this.target = target;
        this.trail = trail;
        this.start = start;
        this.collectedEggs = collectedEggs;
        this.collectedTargets = collectedTargets;
        this.avoidTargets = avoidTargets;
        open.put(start, new AStarNode(null, start, 0, 0));
    }

    public static Point getNeighbor(Point p, Direction dir) {
        switch(dir){
            case UP: return new Point(p.x, p.y - 1);
            case RIGHT: return new Point(p.x + 1, p.y);
            case DOWN: return new Point(p.x, p.y + 1);
            case LEFT: return new Point(p.x - 1, p.y);
            case NONE:
            default: return p;
        }
    }

    public static int getEuclidDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private boolean isBlocked(AStarNode from, Point to) {
        Direction dir = grid.getDirection(from.getP(), to);
        if (dir == Direction.NONE) {
            return true;
        }
        Cell toV = grid.getValue(to);
        if (toV.getType() == CellType.WALL || toV.getType() == CellType.SPIKY_BOI) {
            return true;
        }
        if (toV.getType() == CellType.CURVE) {
            return toV.getDirection() == dir || toV.getDirection().next() == dir;
        }
        if (toV.getType() == CellType.PORTAL) {
            return toV.getDirection() != dir.next().next();
        }
        if (toV.getType() == CellType.TARGET && (avoidTargets || (!collectedEggs.isEmpty() && !collectedTargets.contains(to) && !to.equals(target)))) {
            return true;
        }
        if (toV.getType() == CellType.EGG && !collectedEggs.contains(to) && !to.equals(target)) {
            return true;
        }
        if (trail.containsKey(to)) {
            return from.getG() <= trail.get(to);
        }
        return false;
    }

    public AStarNode getNextLowestNode() {
        if (open.isEmpty()) {
            return null;
        }
        Map.Entry<Point, AStarNode> min = null;
        for (Map.Entry<Point, AStarNode> node : open.entrySet()) {
            if (min == null || node.getValue().getF() < min.getValue().getF()) {
                min = node;
            }
        }
        return open.remove(min.getKey());
    }

    public List<Point> solve(){
        while (!open.isEmpty()){
            AStarNode p = getNextLowestNode();

            for (Direction dir : Direction.cardinals()) {
                Point nextPoint = getNeighbor(p.getP(), dir);
                if (nextPoint.x < 0 || nextPoint.y < 0 || nextPoint.x >= grid.getWidth() ||
                    nextPoint.y >= grid.getHeight() || isBlocked(p, nextPoint)) {
                    continue;
                }
                AStarNode next = new AStarNode(p, nextPoint, p.getG() + 1, getEuclidDistance(nextPoint, target));
                if (target.equals(next.getP())) {
                    //path found
                    List<Point> path = new ArrayList<>();
                    AStarNode n = next;
                    Point np;
                    while (!(np = n.getP()).equals(start)) {
                        path.add(0, new Point(np));
                        n = n.getParent();
                    }
                    return path;
                }
                if ((open.containsKey(nextPoint) && open.get(nextPoint).getF() < next.getF()) ||
                    (closed.containsKey(nextPoint) && closed.get(nextPoint).getF() < next.getF())) {
                    continue;
                }
                open.put(nextPoint, next);
            }
            closed.put(p.getP(), p);
        }
        //no path
        return Collections.emptyList();
    }
}
