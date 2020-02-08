package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.World;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class PathVariableListener implements VariableListener<Visit> {
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
        World w = ((LevelSolution)scoreDirector.getWorkingSolution()).getWorld();
        PlanningNode previousNode = visit.getPreviousNode();
        //if (previousNode instanceof)
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Visit visit) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Visit visit) {

    }
}
