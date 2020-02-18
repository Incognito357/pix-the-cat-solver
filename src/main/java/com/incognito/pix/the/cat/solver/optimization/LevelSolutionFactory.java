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
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class LevelSolutionFactory {
    private LevelSolutionFactory() {}

    public static LevelSolution create(World world, int lvl) {
        LevelSolution solution = new LevelSolution();
        Level level = world.get(Math.max(0, Math.min(world.size() - 1, lvl)));
        solution.setLevel(level);
        Grid<Cell> grid = level.getGrid();
        int numTargets = 0;
        for (List<Grid<Cell>.LinkedCell> linkedCells : grid.getGrid()) {
            for (Grid<Cell>.LinkedCell linkedCell : linkedCells) {
                Cell cell = linkedCell.getValue();
                if (cell.getType() == CellType.PLAYER_START) {
                    solution.getStarts().add(new StartLocation(new Point(linkedCell.getPos()), Direction.NONE));
                } else if (cell.getType() == CellType.EGG) {
                    solution.getEggs().add(new Visit(new Point(linkedCell.getPos()), cell.getType()));
                    numTargets++;
                } else if (cell.getType() == CellType.TARGET) {
                    solution.getTargets().add(new Visit(new Point(linkedCell.getPos()), cell.getType()));
                } else if (cell.getType() == CellType.PORTAL) {
                    if (level.getParentLinks().containsKey(linkedCell.getPos())) {
                        solution.getStarts().add(new StartLocation(linkedCell.getPos(), cell.getDirection()));
                    } else {//if (level.getChildLinks().containsKey(linkedCell.getPos())) {
                        solution.getExits().add(new ExitLocation(new Point(linkedCell.getPos()), cell.getDirection()));
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
