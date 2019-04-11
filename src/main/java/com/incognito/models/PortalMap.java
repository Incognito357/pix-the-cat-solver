package com.incognito.models;

import java.util.HashMap;
import java.util.Map;

public class PortalMap {
    private final Map<CellTarget, CellTarget> in = new HashMap<>();
    private final Map<CellTarget, CellTarget> out = new HashMap<>();

    public void addLink(CellTarget source, CellTarget dest) {
        in.put(source, dest);
        out.put(dest, source);
    }

    public boolean hasLink(CellTarget target) {
        return in.containsKey(target) || out.containsKey(target);
    }

    public CellTarget getTarget(CellTarget source) {
        if (in.containsKey(source)) return in.get(source);
        else return out.getOrDefault(source, null);
    }
}
