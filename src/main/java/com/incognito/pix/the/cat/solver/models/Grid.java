package com.incognito.pix.the.cat.solver.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.incognito.pix.the.cat.solver.models.enums.Direction;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Operations for a 2D grid.
 * @author Incognito357
 * @param <T> The type of data to store
 */
public class Grid<T> {
    private int width = 0;
    private int height = 0;
    private final Map<Point, LinkedCell> gridMap = new HashMap<>();

    public class LinkedCell {
        private Point pos;
        private LinkedCell up;
        private LinkedCell down;
        private LinkedCell right;
        private LinkedCell left;
        private T value;

        private LinkedCell(T value) {
            this.value = value;
        }

        boolean hasLeft() {
            return left != null;
        }

        private boolean hasRight() {
            return right != null;
        }

        private boolean hasUp() {
            return up != null;
        }

        private boolean hasDown() {
            return down != null;
        }

        public Point getPos() {
            return pos;
        }

        public T getValue() {
            return value;
        }
    }

    private LinkedCell getCell(Point point) {
        LinkedCell linkedCell = gridMap.get(point);
        if (linkedCell == null) {
            throw new IndexOutOfBoundsException("Coordinates [" + point.x + "," + point.y + "] out of bounds");
        }
        return linkedCell;
    }

    private LinkedCell getCell(int x, int y) {
        return getCell(new Point(x, y));
    }

    private LinkedCell putCell(Point point, T value) {
        LinkedCell c = new LinkedCell(value);
        c.pos = point;
        return gridMap.put(point, c);
    }

    private LinkedCell putCell(int x, int y, T value) {
        return putCell(new Point(x, y), value);
    }

    public T getValue(Point point) {
        return getCell(point).value;
    }

    public T getValue(int x, int y) {
        return getValue(new Point(x, y));
    }

    public List<T> getNeighbors(Point point) {
        List<T> neighbors = new ArrayList<>();
        LinkedCell linkedCell = getCell(point);
        if (linkedCell.hasUp()) neighbors.add(linkedCell.up.value);
        if (linkedCell.hasRight()) neighbors.add(linkedCell.right.value);
        if (linkedCell.hasDown()) neighbors.add(linkedCell.down.value);
        if (linkedCell.hasLeft()) neighbors.add(linkedCell.left.value);
        return neighbors;
    }

    public List<T> getNeighbors(int x, int y) {
        return getNeighbors(new Point(x, y));
    }

    public void addRow(Supplier<T> defaultValue) {
        if (width == 0) {
            throw new IllegalArgumentException("Cannot infer width of empty grid!");
        }
        List<T> row = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            row.add(defaultValue.get());
        }
        addRow(row);
    }

    public void addRow(List<T> values) {
        int x = 0;
        for (T value : values) {
            putCell(x++, height, value);
            if (width != 0 && x >= width) {
                break;
            }
        }
        if (width == 0) width = x;
        for (x = 0; x < width; x++) {
            LinkedCell c = getCell(x, height);
            if (height > 0) {
                c.up = getCell(x, height - 1);
                c.up.down = c;
            }
            if (x > 0) c.left = getCell(x - 1, height);
            if (x < width - 1) c.right = getCell(x + 1, height);
        }
        height++;
    }

    public void addColumn(Supplier<T> defaultValue) {
        if (height == 0) {
            throw new IllegalArgumentException("Cannot infer height of empty grid!");
        }
        List<T> col = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            col.add(defaultValue.get());
        }
        addColumn(col);
    }

    public void addColumn(List<T> values) {
        int y = 0;
        for (T value : values) {
            putCell(width, y++, value);
            if (height != 0 && y >= height) {
                break;
            }
        }
        if (height == 0) height = y;
        for (y = 0; y < height; y++) {
            LinkedCell c = getCell(width, y);
            if (width > 0) {
                c.left = getCell(width - 1, y);
                c.left.right = c;
            }
            if (y > 0) c.up = getCell(width, y - 1);
            if (y < height - 1) c.down = getCell(width, y + 1);
        }
        width++;
    }

    public void removeRow() {
        if (height > 0) {
            for (int x = 0; x < width; x++) {
                LinkedCell c = gridMap.remove(new Point(x, height - 1));
                if (c.hasUp()) c.up.down = null;
            }
            height--;
            if (height == 0) {
                width = 0;
            }
        }
    }

    public void removeColumn() {
        if (width > 0) {
            for (int y = 0; y < height; y++) {
                LinkedCell c = gridMap.remove(new Point(width - 1, y));
                if (c.hasLeft()) c.left.right = null;
            }
            width--;
            if (width == 0) {
                height = 0;
            }
        }
    }

    public void clear() {
        gridMap.clear();
        width = 0;
        height = 0;
    }

    public List<T> getRowValues(int row) {
        LinkedCell c = getCell(0, row);
        List<T> data = new ArrayList<>();
        for (int x = 0 ; x < width; x++) {
            data.add(c.value);
            c = c.right;
        }
        return data;
    }

    public List<T> getColumnValues(int column) {
        LinkedCell c = getCell(column, 0);
        List<T> data = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            data.add(c.value);
            c = c.down;
        }
        return data;
    }

    @JsonGetter("grid")
    public List<List<T>> exportGridValues() {
        List<List<T>> data = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            data.add(getRowValues(y));
        }
        return data;
    }

    public List<LinkedCell> getRow(int row) {
        LinkedCell c = getCell(0, row);
        List<LinkedCell> data = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            data.add(c);
            c = c.right;
        }
        return data;
    }

    public List<LinkedCell> getColumn(int column) {
        LinkedCell c = getCell(column, 0);
        List<LinkedCell> data = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            data.add(c);
            c = c.down;
        }
        return data;
    }

    @JsonSetter("grid")
    public void setGrid(List<List<T>> data) {
        clear();
        for (List<T> row : data) {
            addRow(row);
        }
    }

    @JsonIgnore
    public List<List<LinkedCell>> getGrid() {
        List<List<LinkedCell>> data = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            data.add(getRow(y));
        }
        return data;
    }

    public T setCell(Point point, T value) {
        LinkedCell c = getCell(point);
        T old = c.value;
        c.value = value;
        return old;
    }

    public T setCell(int x, int y, T value) {
        return setCell(new Point(x, y), value);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Direction getDirection(Point from, Point to) {
        LinkedCell c1 = getCell(from);
        LinkedCell c2 = getCell(to);
        if (c1.hasUp() && c1.up.equals(c2)) return Direction.UP;
        if (c1.hasRight() && c1.right.equals(c2)) return Direction.RIGHT;
        if (c1.hasDown() && c1.down.equals(c2)) return Direction.DOWN;
        if (c1.hasLeft() && c1.left.equals(c2)) return Direction.LEFT;

        throw new IllegalArgumentException("Points [" + from.x + "," + from.y + "] and [" + from.x + "," + from.y + "] are not neighbors!");
    }
}
