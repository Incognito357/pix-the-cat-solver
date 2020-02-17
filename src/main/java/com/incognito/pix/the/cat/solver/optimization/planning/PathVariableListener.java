package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.World;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.optimization.astar.AStarSolver;
import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathVariableListener implements VariableListener<Visit> {
    private static final Logger logger = LoggerFactory.getLogger(PathVariableListener.class);

    private void updateEggsCollected(ScoreDirector scoreDirector, Visit visit, Set<Point> collected) {
        scoreDirector.beforeVariableChanged(visit, "collectedEggs");
        visit.setCollectedEggs(collected);
        scoreDirector.afterVariableChanged(visit, "collectedEggs");
    }

    private void updateTargetsCollected(ScoreDirector scoreDirector, Visit visit, Set<Point> collected) {
        scoreDirector.beforeVariableChanged(visit, "collectedTargets");
        visit.setCollectedTargets(collected);
        scoreDirector.afterVariableChanged(visit, "collectedTargets");
    }

    private void updateTail(ScoreDirector scoreDirector, Visit visit, Map<Point, Integer> tail) {
        scoreDirector.beforeVariableChanged(visit, "tail");
        visit.setTail(tail);
        scoreDirector.afterVariableChanged(visit, "tail");
    }

    private void updateValid(ScoreDirector scoreDirector, Visit visit, boolean value) {
        scoreDirector.beforeVariableChanged(visit, "validPath");
        visit.setValidPath(value);
        scoreDirector.afterVariableChanged(visit, "validPath");
    }

    private void updateAllEggs(ScoreDirector scoreDirector, Visit visit, boolean value) {
        scoreDirector.beforeVariableChanged(visit, "eggsCollected");
        visit.setEggsCollected(value);
        scoreDirector.afterVariableChanged(visit, "eggsCollected");
    }

    private void updatePath(ScoreDirector scoreDirector, Visit visit, List<Point> path) {
        scoreDirector.beforeVariableChanged(visit, "path");
        visit.setPath(path);
        scoreDirector.afterVariableChanged(visit, "path");
    }

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Visit visit) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Visit visit) {

    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Visit visit) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Visit visit) {
        LevelSolution solution = (LevelSolution) scoreDirector.getWorkingSolution();
        World world = solution.getWorld();
        Standstill previousStandstill = visit.getPreviousStandstill();
        Map<Point, Integer> previousTail;
        if (previousStandstill == null) {
            invalidateChain(scoreDirector, visit);
            return;
        }
        Set<Point> eggsCollected;
        Set<Point> targetsCollected;
        boolean avoidTargets = true;
        if (previousStandstill instanceof StartLocation) {
            previousTail = Collections.emptyMap();
            if (visit.getCellType() == CellType.EGG) {
                eggsCollected = new HashSet<>();
                targetsCollected = new HashSet<>();
            } else {
                invalidateChain(scoreDirector, visit);
                return;
            }
        } else {
            Visit previousVisit = (Visit) previousStandstill;
            if (Boolean.FALSE.equals(previousVisit.getValidPath())) {
                invalidateChain(scoreDirector, visit);
                return;
            }
            previousTail = previousVisit.getTail();
            eggsCollected = previousVisit.getCollectedEggs();
            targetsCollected = previousVisit.getCollectedTargets();
            avoidTargets = !previousVisit.getEggsCollected() && previousVisit.getNumCollected() == 0;
        }
        astar(scoreDirector, visit, world, previousTail, eggsCollected, targetsCollected, avoidTargets);
        updateHelperStats(scoreDirector, visit, solution, previousStandstill);
        Visit chain = visit.getNextVisit();
        Visit last = visit;
        while (chain != null) {
            astar(scoreDirector, chain, world, last.getTail(), last.getCollectedEggs(), last.getCollectedTargets(), !last.getEggsCollected());
            updateHelperStats(scoreDirector, chain, solution, last);
            if (Boolean.FALSE.equals(chain.getValidPath())) {
                invalidateChain(scoreDirector, chain);
                return;
            }
            last = chain;
            chain = chain.getNextVisit();
        }
    }

    private void invalidateChain(ScoreDirector scoreDirector, Visit visit) {
        while (visit != null) {
            updateEggsCollected(scoreDirector, visit, new HashSet<>());
            updateTargetsCollected(scoreDirector, visit, new HashSet<>());
            updatePath(scoreDirector, visit, Collections.emptyList());
            updateTail(scoreDirector, visit, Collections.emptyMap());
            updateValid(scoreDirector, visit, false);
            updateAllEggs(scoreDirector, visit, false);
            visit = visit.getNextVisit();
        }
    }

    private void astar(ScoreDirector scoreDirector, Visit visit, World world, Map<Point, Integer> previousTail,
                       Set<Point> collectedEggs, Set<Point> collectedTargets, boolean avoidTargets) {
        Standstill previousStandstill = visit.getPreviousStandstill();
        AStarSolver solver = new AStarSolver(
                world.get(0).getGrid(),
                previousStandstill.getLocation(),
                visit.getLocation(),
                previousTail,
                collectedEggs,
                collectedTargets,
                avoidTargets);
        List<Point> path = solver.solve();
        updatePath(scoreDirector, visit, path);
        if (visit.getCellType() == CellType.EGG) {
            Set<Point> eggs = new HashSet<>(collectedEggs);
            eggs.add(visit.getLocation());
            updateEggsCollected(scoreDirector, visit, eggs);
            updateTargetsCollected(scoreDirector, visit, collectedTargets);
        } else if (visit.getCellType() == CellType.TARGET) {
            Set<Point> targets = new HashSet<>(collectedTargets);
            targets.add(visit.getLocation());
            updateTargetsCollected(scoreDirector, visit, targets);
            updateEggsCollected(scoreDirector, visit, collectedEggs);
        }
        if (path.isEmpty()) {
            invalidateChain(scoreDirector, visit);
            return;
        }
        Map<Point, Integer> tail = new HashMap<>();

        Standstill backtrack = visit;
        boolean first = true;
        while (tail.size() < visit.getNumCollected() && backtrack instanceof Visit) {
            path = ((Visit) backtrack).getPath();
            for (int i = path.size() - (first ? 2 : 1); i >= 0 && tail.size() < visit.getNumCollected(); i--) {
                tail.put(path.get(i), visit.getNumCollected() - tail.size());
            }
            backtrack = ((Visit) backtrack).getPreviousStandstill();
            first = false;
        }
//        if (tail.size() < visit.getNumCollected()) {
//            logger.info("Could not make tail correct length");
//        }
        updateTail(scoreDirector, visit, tail);
    }

    private void updateHelperStats(ScoreDirector scoreDirector, Visit visit, LevelSolution solution, Standstill lastStandstill) {
        if (lastStandstill instanceof StartLocation) {
            updateValid(scoreDirector, visit, visit.getCellType() == CellType.EGG);
            updateAllEggs(scoreDirector, visit, visit.getCellType() == CellType.EGG && solution.getNumTargets() == 1);
            return;
        }
        Visit lastVisit = (Visit)lastStandstill;

        if (visit.getCellType() == CellType.EGG) {
            if (lastVisit.getCellType() == CellType.TARGET) {
                updateValid(scoreDirector, visit, false);
            } else {
                updateValid(scoreDirector, visit, lastVisit.getValidPath());
            }
            updateAllEggs(scoreDirector, visit, visit.getNumCollected() == solution.getNumTargets());
        } else if (visit.getCellType() == CellType.TARGET) {
            if (lastVisit.getCellType() == CellType.EGG) {
                updateValid(scoreDirector, visit, lastVisit.getValidPath() && lastVisit.getEggsCollected());
            } else {
                updateValid(scoreDirector, visit, lastVisit.getValidPath());
            }
            updateAllEggs(scoreDirector, visit, lastVisit.getEggsCollected());
        } else {
            updateValid(scoreDirector, visit, lastVisit.getValidPath());
            updateAllEggs(scoreDirector, visit, lastVisit.getEggsCollected());
        }
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Visit visit) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Visit visit) {

    }
}
