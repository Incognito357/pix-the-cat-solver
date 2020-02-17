package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.World;
import java.util.ArrayList;
import java.util.List;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class LevelSolution {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "eggs")
    private List<Visit> eggs = new ArrayList<>();

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "targets")
    private List<Visit> targets = new ArrayList<>();

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "starts")
    private List<StartLocation> starts = new ArrayList<>();

    @ProblemFactProperty
    @ValueRangeProvider(id = "exits")
    private List<ExitLocation> exits = new ArrayList<>();

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "path")
    private List<Visit> path = new ArrayList<>();

    @ProblemFactProperty
    private World world;

    @ProblemFactProperty
    private int numTargets;

    @PlanningScore
    private HardSoftScore score;

    public List<StartLocation> getStarts() {
        return starts;
    }

    public void setStarts(List<StartLocation> starts) {
        this.starts = starts;
    }

    public List<Visit> getEggs() {
        return eggs;
    }

    public void setEggs(List<Visit> eggs) {
        this.eggs = eggs;
    }

    public List<Visit> getTargets() {
        return targets;
    }

    public void setTargets(List<Visit> targets) {
        this.targets = targets;
    }

    public List<ExitLocation> getExits() {
        return exits;
    }

    public void setExits(List<ExitLocation> exits) {
        this.exits = exits;
    }

    public List<Visit> getPath() {
        return path;
    }

    public void setPath(List<Visit> path) {
        this.path = path;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public int getNumTargets() {
        return numTargets;
    }

    public void setNumTargets(int numTargets) {
        this.numTargets = numTargets;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
