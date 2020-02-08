package com.incognito.pix.the.cat.solver.gui;

import com.incognito.pix.the.cat.solver.models.Cell;
import com.incognito.pix.the.cat.solver.models.Grid;
import com.incognito.pix.the.cat.solver.models.Level;
import com.incognito.pix.the.cat.solver.models.enums.CellType;
import com.incognito.pix.the.cat.solver.models.enums.Direction;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;

public class EditorPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private int cellSize = 16;
    static final int DEFAULT_WIDTH = 15, DEFAULT_HEIGHT = 10;
    private Point origin = new Point(0, 0);
    private Point dragOrigin = new Point(0, 0);
    private Point hoverCell = new Point(0, 0);
    private Point selectOrigin = new Point(0, 0);
    private Point selectDrag = new Point(0, 0);
    private boolean rclick = false, panning = false, selecting = false, selected = false;
    private Grid<Cell> grid = new Grid<>();
    private Level level = new Level("", grid);
    private ClickMode clickMode = ClickMode.DRAW;
    private CellType drawMode = CellType.EMPTY;
    private Point copyOrigin = new Point(0, 0);
    private Point copyDrag = new Point(0, 0);
    private boolean isCut = false, isCopy = false;
    private List<List<Cell>> clipboard = new ArrayList<>();
    private String levelName = "";

    enum ClickMode {
        DRAW,
        ERASE,
        SELECT,
        FILL
    }

    public EditorPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
    }

    private void drawPoint(Graphics g, Point p, String label, int x, int y) {
        g.drawString(label + ": " + p.x + ", " + p.y, x, y);
    }

    private boolean inGrid(Point p) {
        return p.x >= 0 && p.x < grid.getWidth() && p.y >= 0 && p.y < grid.getHeight();
    }

    private void scale(Point p1, int factor) {
        p1.setLocation(p1.x * factor, p1.y * factor);
    }

    private Point gridToScreen(Point p) {
        scale(p, cellSize);
        p.translate(origin.x, origin.y);
        return p;
    }

    private void centerLevel() {
        if (grid.getWidth() > 0 && grid.getHeight() > 0) {
            origin.x = (this.getWidth() / 2) - ((grid.getWidth() * cellSize) / 2);
            origin.y = (this.getHeight() / 2) - ((grid.getHeight() * cellSize) / 2);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(origin.x, origin.y, grid.getWidth() * cellSize, grid.getHeight() * cellSize);
        g.setColor(Color.DARK_GRAY);

        boolean hideGrid = cellSize <= 3;
        if (!hideGrid) {
            for (int y = 0; y <= grid.getHeight(); y++) {
                Point p1 = gridToScreen(new Point(0, y));
                Point p2 = gridToScreen(new Point(grid.getWidth(), y));
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            for (int x = 0; x <= grid.getWidth(); x++) {
                Point p1 = gridToScreen(new Point(x, 0));
                Point p2 = gridToScreen(new Point(x, grid.getHeight()));
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        int innerCellSize = hideGrid ? cellSize : cellSize - 1;
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                Point p = new Point(x, y);
                Cell cell = grid.getValue(x, y);
                CellType t = cell.getType();
                if (t != CellType.EMPTY) {
                    Color c = Color.DARK_GRAY;
                    switch (t) {
                        case EGG:
                            c = new Color(64, 192, 96);
                            break;
                        case TARGET:
                            c = new Color(224, 192, 32);
                            break;
                        case PORTAL:
                            if (level.getChildLinks().containsKey(p)) {
                                c = new Color(64, 64, 128);
                            } else if (level.getParentLinks().containsKey(p)) {
                                c = new Color(64, 128, 64);
                            } else {
                                c = new Color(255, 64, 64);
                            }
                            break;
                        case PLAYER_START:
                            c = new Color(64, 64, 192);
                            break;
                        case SPIKY_BOI:
                            c = new Color(192, 64, 64);
                            break;
                        default:
                            break;
                    }
                    Point p1 = gridToScreen(new Point(x, y));
                    if (!hideGrid) {
                        p1.x = p1.x + 1;
                        p1.y = p1.y + 1;
                    }
                    g.setColor(c);
                    g.fillRect(p1.x, p1.y, innerCellSize, innerCellSize);
                    if (t == CellType.PORTAL) {
                        g.setColor(Color.DARK_GRAY);
                        Direction dir = cell.getDirection();
                        double partialSize = 0.6;
                        int dirSize = (int) Math.ceil(innerCellSize * partialSize);
                        int px = p1.x + (dir == Direction.LEFT ? (int) (innerCellSize * (1 - partialSize)) : 0);
                        int py = p1.y + (dir == Direction.UP ? (int) (innerCellSize * (1 - partialSize)) : 0);
                        int cx = dir == Direction.RIGHT || dir == Direction.LEFT ? dirSize : innerCellSize;
                        int cy = dir == Direction.UP || dir == Direction.DOWN ? dirSize : innerCellSize;
                        g.fillRect(px, py, cx, cy);
                    } else if (t == CellType.CURVE) {
                        g.setColor(Color.LIGHT_GRAY);
                        Direction dir = cell.getDirection();
                        double start = -90 * ((dir.ordinal() + 1) - Direction.UP.ordinal());
                        Arc2D.Double arc = new Arc2D.Double(
                                p1.x - (dir == Direction.LEFT || dir == Direction.DOWN ? cellSize - 1 : 0),
                                p1.y - (dir == Direction.UP || dir == Direction.LEFT ? cellSize - 1 : 0),
                                innerCellSize * 2, innerCellSize * 2, start, -90, Arc2D.PIE);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.fill(arc);
                    }
                }
            }
        }

        switch (clickMode) {
            case DRAW:
                g.setColor(new Color(192, 255, 192, 128));
                break;
            case ERASE:
                g.setColor(new Color(255, 192, 192, 128));
                break;
            case SELECT:
                break;
        }
        if (inGrid(hoverCell)) {
            if (clickMode != ClickMode.SELECT) {
                g.fillRect(hoverCell.x * cellSize + origin.x + 1, hoverCell.y * cellSize + origin.y + 1, cellSize - 1, cellSize - 1);
            } else {
                if (!selecting) {
                    g.setColor(new Color(255, 255, 255, 224));
                    g.drawRect(hoverCell.x * cellSize + origin.x, hoverCell.y * cellSize + origin.y, cellSize, cellSize);
                }
            }
        }

        if (selecting || selected || isCut || isCopy) {
            Color c1;
            Color c2;
            if (selecting || selected) {
                c1 = new Color(255, 255, 255, 128);
                c2 = new Color(255, 255, 255);
            } else {
                c1 = new Color(192, 192, 255, 128);
                c2 = new Color(192, 192, 255);
            }
            g.setColor(c1);
            Point p1 = gridToScreen(new Point(Math.min(selectOrigin.x, selectDrag.x), Math.min(selectOrigin.y, selectDrag.y)));
            Point p2 = gridToScreen(new Point(Math.abs(selectOrigin.x - selectDrag.x), Math.abs(selectOrigin.y - selectDrag.y)));
            p2.translate(-origin.x, -origin.y);
            if (selectDrag.x <= selectOrigin.x) p2.x += cellSize;
            if (selectDrag.y <= selectOrigin.y) p2.y += cellSize;
            g.fillRect(p1.x + 1, p1.y + 1, p2.x - 1, p2.y - 1);
            g.setColor(c2);
            g.drawRect(p1.x, p1.y, p2.x, p2.y);
        }

        if (grid.getWidth() > 0 && grid.getHeight() > 0) {
            boolean haveEntrance = false;
            boolean haveExit = false;
            int missingTargets = 0;
            int entranceInvalid = 0;
            int exitInvalid = 0;
            int eggBalance = 0;
            Point tmpTarget = new Point(0, 0);
            for (int y = 0; y < grid.getHeight(); y++) {
                List<Cell> row = grid.getRow(y);
                for (int x = 0; x < grid.getWidth(); x++) {
                    Cell c = row.get(x);
                    CellType type = c.getType();
                    if (type == CellType.PORTAL) {
                        Point entranceTarget = level.getParentLinks().get(tmpTarget);
                        Point exitTarget = level.getChildLinks().get(tmpTarget);
                        if (entranceTarget == null && exitTarget == null) {
                            missingTargets++;
                        } else {
                            if (level.getParent().getChildLinks().containsKey(tmpTarget) &&
                                    level.getParent().getGrid().getValue(entranceTarget).getDirection().next().next() == level.getGrid().getValue(tmpTarget).getDirection()) {
                                haveEntrance = true;
                            } else {
                                entranceInvalid++;
                            }

                            if (level.getChild().getParentLinks().containsKey(tmpTarget) &&
                                    level.getChild().getGrid().getValue(exitTarget).getDirection().next().next() == level.getGrid().getValue(tmpTarget).getDirection()) {
                                haveExit = true;
                            } else {
                                exitInvalid++;
                            }
                        }
                    } else if (type == CellType.EGG) {
                        eggBalance++;
                    } else if (type == CellType.TARGET) {
                        eggBalance--;
                    }
                    tmpTarget.x++;
                }
                tmpTarget.y++;
                tmpTarget.x = 0;
            }

            int p = 0;
            if (eggBalance != 0) {
                String s = "s";
                if (Math.abs(eggBalance) == 1) {
                    s = "";
                }
                drawString(g, Color.RED, String.format(
                        "There %s %d more %s than %s!",
                        s.length() == 0 ? "is" : "are",
                        Math.abs(eggBalance),
                        (eggBalance < 0 ? "target" : "egg") + s,
                        (eggBalance < 0 ? "egg" : "target") + s),
                        new Point(5, p += 15));
            }
            if (!haveEntrance && level.getParent() != null) {
                drawString(g, Color.RED, "There is no entrance portal!", new Point(5, p += 15));
            }
            if (!haveExit && level.getChild() != null) {
                drawString(g, Color.RED, "There is no exit portal!", new Point(5, p += 15));
            }
            if (missingTargets > 0) {
                String s = "s";
                if (missingTargets == 1) {
                    s = "";
                }
                drawString(g, Color.YELLOW, String.format(
                        "There %s %d portal%s in this level without %starget%s!",
                        (missingTargets == 1 ? "is" : "are"),
                        missingTargets, s, missingTargets == 1 ? "a " : "", s),
                        new Point(5, p += 15));
            }
            if (entranceInvalid > 0) {
                String s = "s";
                if (entranceInvalid == 1) {
                    s = "";
                }
                drawString(g, Color.YELLOW, String.format(
                        "There %s %d entrance portal%s in this level that are invalid! (check parent level and direction)",
                        (entranceInvalid == 1 ? "is" : "are"),
                        entranceInvalid, s),
                        new Point(5, p += 15));
            }
            if (exitInvalid > 0) {
                String s = "s";
                if (exitInvalid == 1) {
                    s = "";
                }
                drawString(g, Color.YELLOW, String.format(
                        "There %s %d exit portal%s in this level that are invalid! (check parent level and direction)",
                        (exitInvalid == 1 ? "is" : "are"),
                        exitInvalid, s),
                        new Point(5, p += 15));
            }
        }
    }

    private void drawString(Graphics g, Color c, String s, Point pos) {
        g.setColor(Color.black);
        int size = 2;
        for (int y = -size; y <= size; y++) {
            for (int x = -size; x <= size; x++) {
                if (x == 0 && y == 0) continue;
                g.drawString(s, pos.x + x, pos.y + y);
            }
        }
        g.setColor(c);
        g.drawString(s, pos.x, pos.y);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        grabFocus();
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (inGrid(hoverCell)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                mouseClick(e.getPoint());
                if (clickMode == ClickMode.SELECT) {
                    selectOrigin = hoverCell;
                    if (isCut || isCopy) {
                        selectDrag = new Point(copyDrag.x, copyDrag.y);
                        selectDrag.translate(selectOrigin.x - copyOrigin.x, selectOrigin.y - copyOrigin.y);
                    } else selectDrag = new Point(selectOrigin.x + 1, selectOrigin.y + 1);
                    selecting = true;
                }
            } else {
                if (clickMode == ClickMode.SELECT) {
                    selected = false;
                }
            }
            repaint();
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            rclick = true;
            dragOrigin.x = e.getX() - origin.x;
            dragOrigin.y = e.getY() - origin.y;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (!panning) {
                CellType a = drawMode;
                drawMode = CellType.EMPTY;
                mouseClick(e.getPoint());
                drawMode = a;
            }
            panning = false;
            rclick = false;
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            if (clickMode == ClickMode.SELECT && selecting) {
                selected = true;
            }
            selecting = false;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (rclick) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            panning = true;
            origin.x = e.getX() - dragOrigin.x;
            origin.y = e.getY() - dragOrigin.y;
            repaint();
        } else {
            Point lastHover = hoverCell;
            mouseMoved(e.getPoint());
            if (selecting || (inGrid(hoverCell) && (lastHover.x != hoverCell.x || lastHover.y != hoverCell.y))) {
                mouseClick(e.getPoint());
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseMoved(e.getPoint());
    }

    private void mouseMoved(Point p) {
        Point lastHover = hoverCell;
        hoverCell = new Point(p.x, p.y);
        hoverCell.translate(-origin.x, -origin.y);
        hoverCell.setLocation(Math.floor(hoverCell.x / (double) cellSize), Math.floor(hoverCell.y / (double) cellSize));
        boolean newIn = inGrid(hoverCell);
        if (inGrid(lastHover) != newIn) {
            repaint();
        } else if (newIn && (lastHover.x != hoverCell.x || lastHover.y != hoverCell.y)) {
            repaint();
        }
    }

    private void mouseClick(Point p) {
        if (clickMode == ClickMode.SELECT) {
            if (selecting) {
                if (isCut || isCopy) {
                    selectOrigin = hoverCell;
                    selectDrag = new Point(copyDrag.x, copyDrag.y);
                    selectDrag.translate(selectOrigin.x - copyOrigin.x, selectOrigin.y - copyOrigin.y);
                } else {
                    selectDrag = new Point(hoverCell.x, hoverCell.y);
                    if (selectDrag.x >= selectOrigin.x) selectDrag.x += 1;
                    if (selectDrag.y >= selectOrigin.y) selectDrag.y += 1;
                    if (selectDrag.x < 0) selectDrag.x = 0;
                    if (selectDrag.y < 0) selectDrag.y = 0;
                    if (selectDrag.x > grid.getWidth()) selectDrag.x = grid.getWidth();
                    if (selectDrag.y > grid.getHeight()) selectDrag.y = grid.getHeight();
                }
            }
        } else {
            CellType cell = clickMode == ClickMode.DRAW ? drawMode : CellType.EMPTY;
            if (selected) {
                Point p1 = new Point(Math.min(selectOrigin.x, selectDrag.x), Math.min(selectOrigin.y, selectDrag.y));
                Point p2 = new Point(Math.abs(selectOrigin.x - selectDrag.x), Math.abs(selectOrigin.y - selectDrag.y));
                for (int y = 0; y < p2.y; y++) {
                    for (int x = 0; x < p2.x; x++) {
                        grid.setCell(x + p1.x, y + p1.y, new Cell(cell));
                    }
                }
            } else {
                Cell last = grid.getValue(hoverCell.x, hoverCell.y);
                if (last.getType() == cell) {
                    last.setDirection(last.getDirection().next());
                } else {
                    grid.setCell(hoverCell.x, hoverCell.y, new Cell(cell));
                }
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Point lastSize = new Point(grid.getWidth() * cellSize, grid.getHeight() * cellSize);
        cellSize -= e.getWheelRotation() * Math.max(1, Math.log(cellSize + 1));
        cellSize = Math.max(Math.min(cellSize, 128), 1);
        Point newSize = new Point(grid.getWidth() * cellSize, grid.getHeight() * cellSize);
        boolean inGrid = inGrid(hoverCell);
        double centerCell = (cellSize / 2.0) / (grid.getWidth() * cellSize);
        double xScale = inGrid ? (hoverCell.x / (double) grid.getWidth()) + centerCell : 0.5;
        double yScale = inGrid ? (hoverCell.y / (double) grid.getHeight()) + centerCell : 0.5;
        origin.translate((int) ((lastSize.x - newSize.x) * xScale), (int) ((lastSize.y - newSize.y) * yScale));
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown()) {
            if (e.getKeyCode() == KeyEvent.VK_C || e.getKeyCode() == KeyEvent.VK_X) {
                isCut = e.getKeyCode() == KeyEvent.VK_X;
                isCopy = e.getKeyCode() == KeyEvent.VK_C;
                copyOrigin = new Point(selectOrigin.x, selectOrigin.y);
                copyDrag = new Point(selectDrag.x, selectDrag.y);
                Point p1 = new Point(Math.min(selectOrigin.x, selectDrag.x), Math.min(selectOrigin.y, selectDrag.y));
                Point p2 = new Point(Math.abs(selectOrigin.x - selectDrag.x), Math.abs(selectOrigin.y - selectDrag.y));
                for (List<Cell> cellTypes : clipboard) {
                    cellTypes.clear();
                }
                clipboard.clear();
                for (int y = 0; y < p2.y; y++) {
                    List<Cell> row = grid.getRow(y + p1.y);
                    List<Cell> copyTo = new ArrayList<>();
                    for (int x = 0; x < p2.x; x++) {
                        copyTo.add(row.get(x + p1.x));
                    }
                    clipboard.add(copyTo);
                }
                repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_V) {
                if (isCut) {
                    Point p1 = new Point(Math.min(copyOrigin.x, copyDrag.x), Math.min(copyOrigin.y, copyDrag.y));
                    Point p2 = new Point(Math.abs(copyOrigin.x - copyDrag.x), Math.abs(copyOrigin.y - copyDrag.y));
                    for (int y = 0; y < p2.y; y++) {
                        for (int x = 0; x < p2.x; x++) {
                            grid.setCell(x + p1.x, y + p1.y, new Cell(CellType.EMPTY));
                        }
                    }
                    isCut = false;
                }

                Point start = new Point(Math.min(selectOrigin.x, selectDrag.x), Math.min(selectOrigin.y, selectDrag.y));
                for (int y = 0; y < clipboard.size(); y++) {
                    if (y + start.y >= grid.getHeight() || y + start.y < 0) continue;
                    List<Cell> row = clipboard.get(y);
                    for (int x = 0; x < row.size(); x++) {
                        if (x + start.x >= grid.getWidth() || x + start.x < 0) continue;
                        grid.setCell(x + start.x, y + start.y, row.get(x));
                    }
                }
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void rotate(boolean clockwise) {
        Point p1, p2;
        Grid<Cell> tmpGrid = new Grid<>();
        if (selected) {
            p1 = new Point(Math.min(selectOrigin.x, selectDrag.x), Math.min(selectOrigin.y, selectDrag.y));
            p2 = new Point(Math.abs(selectOrigin.x - selectDrag.x), Math.abs(selectOrigin.y - selectDrag.y));
            if (p2.x != p2.y) {
                //TODO: overwrite cells outside selection, if rotating rectangle?
                return;
            }
        } else {
            p1 = new Point(0, 0);
            p2 = new Point(grid.getWidth(), grid.getHeight());
        }
        if (clockwise) {
            for (int y = p2.y - 1; y >= 0; y--) {
                List<Cell> row = new ArrayList<>();
                for (int x = 0; x < p2.x; x++) {
                    Cell c = grid.getValue(x + p1.x, y + p1.y);
                    if (c.getType().isRotateable()) {
                        c.setDirection(c.getDirection().next());
                    }
                    row.add(c);
                }
                tmpGrid.addColumn(row);
            }
        } else {
            for (int x = p2.x - 1; x >= 0; x--) {
                List<Cell> col = new ArrayList<>();
                for (int y = 0; y < p2.y; y++) {
                    Cell c = grid.getValue(x + p1.x, y + p1.y);
                    if (c.getType().isRotateable()) {
                        c.setDirection(c.getDirection().prev());
                    }
                    col.add(c);
                }
                tmpGrid.addRow(col);
            }
        }
        if (selected && (p2.x != grid.getWidth() || p2.y == grid.getHeight())) {
            for (int y = 0; y < p2.y; y++) {
                for (int x = 0; x < p2.x; x++) {
                    grid.setCell(x + p1.x, y + p1.y, tmpGrid.getValue(x, y));
                }
            }
        } else {
            grid = tmpGrid;
            centerLevel();
        }
        repaint();
    }

    public void swap() {
        for (List<Cell> row : grid.getGrid()) {
            for (Cell c : row) {
                if (c.getType() == CellType.EGG) {
                    c.setType(CellType.TARGET);
                } else if (c.getType() == CellType.TARGET) {
                    c.setType(CellType.EGG);
                }
            }
        }
        repaint();
    }

    public void setGridWidth(int width) {
        while (width != grid.getWidth()) {
            if (width > grid.getWidth()) {
                grid.addColumn(() -> new Cell(CellType.EMPTY));
            } else {
                grid.removeColumn();
            }
            repaint();
        }
    }

    public void setGridHeight(int height) {
        while (height != grid.getHeight()) {
            if (height > grid.getHeight()) {
                grid.addRow(() -> new Cell(CellType.EMPTY));
            } else {
                grid.removeRow();
            }
            repaint();
        }
    }

    public void setClickMode(ClickMode mode) {
        clickMode = mode;
        isCut = false;
        isCopy = false;
    }

    public void setDrawMode(CellType mode) {
        drawMode = mode;
    }

    public int getCellSize() {
        return cellSize;
    }

    public ClickMode getClickMode() {
        return clickMode;
    }

    public CellType getDrawMode() {
        return drawMode;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.grid = level.getGrid();
        centerLevel();
        repaint();
    }
}
