package com.incognito.test;

import com.incognito.pix.the.cat.solver.models.enums.Direction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DirectionTest {
    @Test
    public void testDirs() {
        assertThat(Direction.UP.next()).isEqualTo(Direction.RIGHT);
        assertThat(Direction.RIGHT.prev()).isEqualTo(Direction.UP);

        assertThat(Direction.RIGHT.next()).isEqualTo(Direction.DOWN);
        assertThat(Direction.DOWN.prev()).isEqualTo(Direction.RIGHT);

        assertThat(Direction.DOWN.next()).isEqualTo(Direction.LEFT);
        assertThat(Direction.LEFT.prev()).isEqualTo(Direction.DOWN);

        assertThat(Direction.LEFT.next()).isEqualTo(Direction.UP);
        assertThat(Direction.UP.prev()).isEqualTo(Direction.LEFT);
    }
}
