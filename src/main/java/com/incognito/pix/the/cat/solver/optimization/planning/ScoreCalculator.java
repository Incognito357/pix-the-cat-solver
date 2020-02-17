package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.optimization.astar.AStarSolver;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

public class ScoreCalculator implements EasyScoreCalculator<LevelSolution> {
    @Override
    public HardSoftScore calculateScore(LevelSolution levelSolution) {
        List<Visit> visits = levelSolution.getPath();
        int hard = 0;
        int soft = 0;
        for (Visit visit : visits) {
            Standstill previousStandstill = visit.getPreviousStandstill();
            if (previousStandstill == null) {
                hard--;
                continue;
            }
            long dist = visit.getDistanceFromPreviousStandstill();
            if (dist == 0) {
                hard--;
            } else {
                soft -= dist;
            }
            if (visit.getNextVisit() == null) {
                for (ExitLocation exit : levelSolution.getExits()) {
                    AStarSolver solver = new AStarSolver(
                            levelSolution.getWorld().get(0).getGrid(),
                            visit.getLocation(), exit.getPoint(), Collections.emptyMap(),
                            visit.getCollectedEggs(), visit.getCollectedTargets(), false);
                    List<Point> solution = solver.solve();
                    if (solution.isEmpty()) {
                        hard--;
                    } else {
                        soft -= solution.size();
                    }
                }
            }
            if (Boolean.FALSE.equals(visit.getValidPath())) {
                hard--;
            }
        }
        return HardSoftScore.of(hard, soft);
    }
}
