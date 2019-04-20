package com.incognito.models;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class World implements Iterable<Level> {
    private Level head;
    private Level tail;
    private int size = 0;

    @Override
    public Iterator<Level> iterator() {
        return new Iterator<Level>() {
            private Level curItem;

            @Override
            public boolean hasNext() {
                return curItem != tail;
            }

            @Override
            public Level next() {
                if (curItem == null) {
                    curItem = head;
                    return curItem;
                }
                if (curItem.getChild() == null) {
                    throw new NoSuchElementException();
                }
                curItem = curItem.getChild();
                return curItem;
            }
        };
    }

    public void addTail(Level level) {
        if (head == null) {
            head = level;
            level.setParent(null);
            level.setChild(null);
        } else {
            tail.setChild(level);
            level.setParent(tail);
            level.setChild(null);
        }
        tail = level;
        size++;
    }

    public void addHead(Level level) {
        if (head == null) {
            tail = level;
            level.setParent(null);
            level.setChild(null);
        } else {
            head.setParent(level);
            level.setParent(null);
            level.setChild(head);
        }
        head = level;
        size++;
    }

    public void insertLevel(Level level, int index) {
        if (index <= 0) {
            addHead(level);
            return;
        } else if (index >= size) {
            addTail(level);
            return;
        }

        int i = 1;
        for (Level l : this) {
            if (i == index) {
                level.setParent(l);
                level.setChild(l.getChild());
                l.getChild().setParent(level);
                l.setChild(level);
                break;
            }
            i++;
        }
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (Level l : this) {
            if (l.getParent() != null) {
                l.getParent().setChild(null);
            }
            l.setParent(null);
        }
        head = null;
        tail = null;
        size = 0;
    }
}
