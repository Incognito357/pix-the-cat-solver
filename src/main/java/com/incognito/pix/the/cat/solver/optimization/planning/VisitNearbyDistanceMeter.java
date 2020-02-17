package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.optimization.astar.AStarSolver;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

public class VisitNearbyDistanceMeter implements NearbyDistanceMeter<Visit, Standstill> {
    @Override
    public double getNearbyDistance(Visit origin, Standstill destination) {
        return AStarSolver.getEuclidDistance(origin.getLocation(), destination.getLocation());
    }
}
