package com.incognito.pix.the.cat.solver.optimization;

import com.incognito.pix.the.cat.solver.models.Cell;
import com.incognito.pix.the.cat.solver.models.Grid;
import com.incognito.pix.the.cat.solver.models.Level;
import com.incognito.pix.the.cat.solver.models.World;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.models.enums.Direction;
import com.incognito.pix.the.cat.solver.optimization.planning.ExitLocation;
import com.incognito.pix.the.cat.solver.optimization.planning.LevelSolution;
import com.incognito.pix.the.cat.solver.optimization.planning.StartLocation;
import com.incognito.pix.the.cat.solver.optimization.planning.Visit;
import java.util.ArrayList;
import java.util.List;

public class LevelSolutionFactory {
    private LevelSolutionFactory() {}

    public static LevelSolution create(World world) {
        LevelSolution solution = new LevelSolution();
        solution.setWorld(world);
        Level level = world.get(0);
        Grid<Cell> grid = level.getGrid();
        int numTargets = 0;
        for (List<Grid<Cell>.LinkedCell> linkedCells : grid.getGrid()) {
            for (Grid<Cell>.LinkedCell linkedCell : linkedCells) {
                Cell cell = linkedCell.getValue();
                if (cell.getType() == CellType.PLAYER_START) {
                    solution.getStarts().add(new StartLocation(linkedCell.getPos(), Direction.NONE));
                } else if (cell.getType() == CellType.EGG) {
                    solution.getEggs().add(new Visit(linkedCell.getPos(), cell.getType()));
                    numTargets++;
                } else if (cell.getType() == CellType.TARGET) {
                    solution.getTargets().add(new Visit(linkedCell.getPos(), cell.getType()));
                } else if (cell.getType() == CellType.PORTAL) {
                    if (level.getParentLinks().containsKey(linkedCell.getPos())) {
                        solution.getStarts().add(new StartLocation(linkedCell.getPos(), cell.getDirection()));
                    } else if (level.getChildLinks().containsKey(linkedCell.getPos())) {
                        solution.getExits().add(new ExitLocation(linkedCell.getPos(), cell.getDirection()));
                    }
                }
            }
        }
        solution.setNumTargets(numTargets);
        List<Visit> path = new ArrayList<>();
        path.addAll(solution.getEggs());
        path.addAll(solution.getTargets());
        solution.setPath(path);
        return solution;
    }
}
