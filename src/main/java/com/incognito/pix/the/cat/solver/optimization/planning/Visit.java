package com.incognito.pix.the.cat.solver.optimization.planning;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import java.awt.Point;
import java.util.Map;

@PlanningEntity
public class Visit implements PlanningNode {

    private Point location;

    @PlanningVariable(
            valueRangeProviderRefs = { "entrances", "eggs", "targets", "path" },
            graphType = PlanningVariableGraphType.CHAINED)
    private PlanningNode previousNode;

    @CustomShadowVariable(
            variableListenerClass = PathVariableListener.class,
            sources = @PlanningVariableReference(variableName = "previousNode"))
    private Map<Point, Integer> path;

    @Override
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public PlanningNode getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(PlanningNode previousNode) {
        this.previousNode = previousNode;
    }
}
