package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.World;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.List;

public class ScoreCalculator implements EasyScoreCalculator<LevelSolution> {
    @Override
    public Score calculateScore(LevelSolution levelSolution) {
        List<Visit> visits = levelSolution.getPath();
        World world = levelSolution.getWorld();
        long hard = 0;
        long soft = 0;
        for (Visit visit : visits) {
            PlanningNode previousNode = visit.getPreviousNode();
            if (previousNode != null) {
                //AStarSolver solver = new AStarSolver(world, )
            }
        }
        return null;
    }
}
