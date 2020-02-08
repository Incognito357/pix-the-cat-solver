package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.World;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.awt.Point;
import java.util.List;

@PlanningSolution
public class LevelSolution {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "entrances")
    private List<Point> entrances;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "eggs")
    private List<Point> eggs;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "targets")
    private List<Point> targets;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "path")
    private List<Visit> path;

    @ProblemFactProperty
    private World world;

    @PlanningScore
    private HardSoftScore score;

    public List<Point> getEntrances() {
        return entrances;
    }

    public void setEntrances(List<Point> entrances) {
        this.entrances = entrances;
    }

    public List<Point> getEggs() {
        return eggs;
    }

    public void setEggs(List<Point> eggs) {
        this.eggs = eggs;
    }

    public List<Point> getTargets() {
        return targets;
    }

    public void setTargets(List<Point> targets) {
        this.targets = targets;
    }

    public List<Visit> getPath() {
        return path;
    }

    public void setPath(List<Visit> path) {
        this.path = path;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
