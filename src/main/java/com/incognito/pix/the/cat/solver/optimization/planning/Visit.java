package com.incognito.pix.the.cat.solver.optimization.planning;

import com.incognito.pix.the.cat.solver.models.enums.CellType;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

@PlanningEntity
public class Visit implements Standstill {
    private Point location;
    private Visit nextVisit;
    private CellType cellType;

    private Standstill previousStandstill;
    private StartLocation startLocation;
    private Map<Point, Integer> tail;
    private Set<Point> collectedEggs;
    private Set<Point> collectedTargets;
    private List<Point> path;
    private boolean validPath = false;
    private boolean eggsCollected = false;

    public Visit() {}

    public Visit(Point location, CellType cellType) {
        this.location = location;
        this.cellType = cellType;
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    @AnchorShadowVariable(sourceVariableName = "previousStandstill")
    public StartLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(StartLocation startLocation) {
        this.startLocation = startLocation;
    }

    @Override
    public Visit getNextVisit() {
        return nextVisit;
    }

    @Override
    public void setNextVisit(Visit nextVisit) {
        this.nextVisit = nextVisit;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    @PlanningVariable(
            valueRangeProviderRefs = { "starts", "eggs", "targets" },
            graphType = PlanningVariableGraphType.CHAINED)
    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(Standstill previousNode) {
        this.previousStandstill = previousNode;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "path"))
    public Map<Point, Integer> getTail() {
        return tail;
    }

    public void setTail(Map<Point, Integer> tail) {
        this.tail = tail;
    }

    @CustomShadowVariable(
            variableListenerClass = PathVariableListener.class,
            sources = @PlanningVariableReference(variableName = "previousStandstill"))
    public List<Point> getPath() {
        return path;
    }

    public void setPath(List<Point> path) {
        this.path = path;
    }

    public long getDistanceFromPreviousStandstill() {
        if (previousStandstill == null) {
            throw new IllegalArgumentException("previousStandstill is not yet initialized");
        }
        return path.size();
    }

    public Integer getNumCollected() {
        return collectedEggs.size() - collectedTargets.size();
    }

    public void setValidPath(boolean validPath) {
        this.validPath = validPath;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "path"))
    public Boolean getValidPath() {
        return validPath;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "path"))
    public Boolean getEggsCollected() {
        return eggsCollected;
    }

    public void setEggsCollected(Boolean eggsCollected) {
        this.eggsCollected = eggsCollected;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "path"))
    public Set<Point> getCollectedEggs() {
        return collectedEggs;
    }

    public void setCollectedEggs(Set<Point> collectedEggs) {
        this.collectedEggs = collectedEggs;
    }

    @CustomShadowVariable(variableListenerRef = @PlanningVariableReference(variableName = "path"))
    public Set<Point> getCollectedTargets() {
        return collectedTargets;
    }

    public void setCollectedTargets(Set<Point> collectedTargets) {
        this.collectedTargets = collectedTargets;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Map.Entry<Point, Integer> entry : tail.entrySet()) {
            joiner.add(String.format("[%d,%d:%d]", entry.getKey().x, entry.getKey().y, entry.getValue()));
        }
        return joiner.toString();
    }
}
