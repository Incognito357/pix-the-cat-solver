package com.incognito.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incognito.pix.the.cat.solver.models.Level;
import com.incognito.pix.the.cat.solver.models.World;
import com.incognito.pix.the.cat.solver.models.serialization.PointKeyDeserializer;
import com.incognito.pix.the.cat.solver.models.serialization.PointKeySerializer;
import com.incognito.pix.the.cat.solver.optimization.astar.AStarSolver;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class AStarTest {

    private static ObjectMapper objectMapper;
    @BeforeClass
    public static void init() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addKeySerializer(Point.class, new PointKeySerializer());
        module.addKeyDeserializer(Point.class, new PointKeyDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void test() throws IOException {
        Map<Point, Integer> trail = new HashMap<>();
        trail.put(new Point(2, 1), 1);
        trail.put(new Point(3, 1), 2);
        trail.put(new Point(4, 1), 3);
        Point start = new Point(4, 2);
        Point end = new Point(5, 1);
        World world;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("testLevel.lvl")) {
            world = importWorld(is);
        }
        AStarSolver solver = new AStarSolver(world.get(0).getGrid(), start, end, trail,
                new HashSet<>(Arrays.asList(
                    new Point(3, 1),
                    new Point(4, 1),
                    new Point(4, 2))),
                Collections.emptySet(), true);
        List<Point> solution = solver.solve();
        assertThat(solution).containsExactly(
                new Point(4, 3),
                new Point(4, 4),
                new Point(3, 4),
                new Point(2, 4),
                new Point(1, 4),
                new Point(1, 3),
                new Point(1, 2),
                new Point(1, 1),
                new Point(2, 1),
                new Point(3, 1),
                new Point(4, 1),
                new Point(5, 1)
        );
    }

    private World importWorld(InputStream is) throws IOException {
        World world = new World();
        List<Level> imported = objectMapper.readValue(is, new TypeReference<List<Level>>() {});
        imported.forEach(world::addTail);
        return world;
    }
}
