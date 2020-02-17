package com.incognito.pix.the.cat.solver.optimization.planning;

import java.awt.Point;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public interface Standstill {
    Point getLocation();

    StartLocation getStartLocation();

    @InverseRelationShadowVariable(sourceVariableName = "previousStandstill")
    Visit getNextVisit();
    void setNextVisit(Visit visit);
}
