package com.incognito.test;

import static com.incognito.pix.the.cat.solver.models.enums.Direction.DOWN;
import static com.incognito.pix.the.cat.solver.models.enums.Direction.LEFT;
import static com.incognito.pix.the.cat.solver.models.enums.Direction.RIGHT;
import static com.incognito.pix.the.cat.solver.models.enums.Direction.UP;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incognito.pix.the.cat.solver.models.Level;
import com.incognito.pix.the.cat.solver.models.World;
import com.incognito.pix.the.cat.solver.models.enums.Direction;
import com.incognito.pix.the.cat.solver.models.serialization.PointKeyDeserializer;
import com.incognito.pix.the.cat.solver.models.serialization.PointKeySerializer;
import com.incognito.pix.the.cat.solver.optimization.astar.AStarSolver;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class AStarTest {

    private static World world;

    @BeforeClass
    public static void init() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addKeySerializer(Point.class, new PointKeySerializer());
        module.addKeyDeserializer(Point.class, new PointKeyDeserializer());
        objectMapper.registerModule(module);

        world = new World();
        try (InputStream is = AStarTest.class.getClassLoader().getResourceAsStream("testLevel.lvl")) {
            List<Level> imported = objectMapper.readValue(is, new TypeReference<List<Level>>() {});
            imported.forEach(world::addTail);
        }
    }

    @Test
    public void test() {
        Map<Point, Integer> trail = new HashMap<>();
        trail.put(new Point(2, 1), 1);
        trail.put(new Point(3, 1), 2);
        trail.put(new Point(4, 1), 3);
        Point start = new Point(4, 2);
        Point end = new Point(5, 1);

        AStarSolver solver = new AStarSolver(world.get(0).getGrid(), start, end, trail,
                new HashSet<>(Arrays.asList(
                    new Point(3, 1),
                    new Point(4, 1),
                    new Point(4, 2))),
                Collections.emptySet(), true);
        List<Point> solution = solver.solve();
        assertThat(solution).containsExactly(makePath(start,
                go(DOWN, 2),
                go(LEFT, 3),
                go(UP, 3),
                go(RIGHT, 4)));
    }

    @Test
    public void testCurves() {
        Map<Point, Integer> trail = new HashMap<>();
        trail.put(new Point(2, 1), 1);
        trail.put(new Point(3, 1), 2);
        trail.put(new Point(4, 1), 3);
        Point start = new Point(4, 2);
        Point end = new Point(5, 1);

        AStarSolver solver = new AStarSolver(world.get(1).getGrid(), start, end, trail,
                new HashSet<>(Arrays.asList(
                        new Point(3, 1),
                        new Point(4, 1),
                        new Point(4, 2))),
                Collections.emptySet(), true);
        List<Point> solution = solver.solve();
        assertThat(solution).containsExactly(makePath(start,
                go(DOWN, 2),
                go(LEFT, 3),
                go(UP, 3),
                go(RIGHT, 4)));
    }

    @Test
    public void testTunnel() {
        Point start = new Point(1, 3);
        Point end = new Point(3, 7);

        AStarSolver solver = new AStarSolver(world.get(2).getGrid(), start, end, Collections.emptyMap(),
                Collections.singleton(new Point(4, 5)), Collections.singleton(new Point(4, 7)), false);
        List<Point> solution = solver.solve();
        assertThat(solution).containsExactly(makePath(start,
                go(UP, 2),
                go(RIGHT, 7),
                go(DOWN, 6),
                go(LEFT, 2),
                go(UP, 2),
                go(LEFT, 5),
                go(DOWN, 3),
                go(RIGHT, 3),
                go(UP, 1),
                go(LEFT, 1)));
    }

    @Test
    public void testFirstPastTarget() {
        Point start = new Point(7, 0);
        Point end = new Point(11, 3);
        AStarSolver solver = new AStarSolver(world.get(3).getGrid(), start, end, Collections.emptyMap(),
                Collections.emptySet(), Collections.emptySet(), false);
        List<Point> solution = solver.solve();
        assertThat(solution).containsExactly(makePath(start,
                go(DOWN, 1),
                go(RIGHT, 2),
                go(DOWN, 2),
                go(RIGHT, 2)));
    }

    @Test
    public void testEndToPortal() {
        Point start = new Point(4, 2);
        Point end = new Point(8, 3);
        AStarSolver solver = new AStarSolver(world.get(3).getGrid(), start, end, Collections.emptyMap(),
                new HashSet<>(Arrays.asList(
                        new Point(10, 2),
                        new Point(11, 2),
                        new Point(12, 2),
                        new Point(11, 3),
                        new Point(12, 3),
                        new Point(7, 4),
                        new Point(8, 4),
                        new Point(9, 4))),
                new HashSet<>(Arrays.asList(
                        new Point(2, 2),
                        new Point(3, 2),
                        new Point(4, 2),
                        new Point(2, 3),
                        new Point(3, 3),
                        new Point(10, 3),
                        new Point(5, 4),
                        new Point(6, 4))),
                false);
        List<Point> solution = solver.solve();
        assertThat(solution).containsExactly(makePath(start,
                go(DOWN, 1),
                go(RIGHT, 1),
                go(DOWN, 1),
                go(RIGHT, 3),
                go(UP, 1)));
    }

    public static Direction[] go(Direction direction, int count) {
        Direction[] filled = new Direction[count];
        Arrays.fill(filled, direction);
        return filled;
    }

    private static Point[] makePath(Point start, Direction[]... directions) {
        List<Point> path = new ArrayList<>();
        Point last = start;
        for (Direction[] dirs : directions) {
            for (Direction d : dirs) {
                last = AStarSolver.getNeighbor(last, d);
                path.add(last);
            }
        }
        return path.toArray(new Point[]{});
    }
}
