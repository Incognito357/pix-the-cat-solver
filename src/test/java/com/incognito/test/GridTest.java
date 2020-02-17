package com.incognito.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.incognito.pix.the.cat.solver.models.Grid;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GridTest {
    private Grid<Integer> grid = new Grid<>();

    private List<Integer> list(Integer... values) {
        return Arrays.asList(values);
    }

    @Test
    public void testOps() {
        assertThat(grid.getHeight()).isEqualTo(0);
        assertThat(grid.getWidth()).isEqualTo(0);

        grid.addRow(list(0, 1, 2, 3));
        assertThat(grid.getHeight()).isEqualTo(1);
        assertThat(grid.getWidth()).isEqualTo(4);
        assertThat(grid.getRowValues(0)).containsExactly(0, 1, 2, 3);

        grid.addRow(list(4, 5, 6, 7));
        assertThat(grid.getHeight()).isEqualTo(2);
        assertThat(grid.getWidth()).isEqualTo(4);
        assertThat(grid.getRowValues(1)).containsExactly(4, 5, 6, 7);

        grid.removeColumn();
        assertThat(grid.getHeight()).isEqualTo(2);
        assertThat(grid.getWidth()).isEqualTo(3);
        assertThat(grid.getRowValues(0)).containsExactly(0, 1, 2);
        assertThat(grid.getRowValues(1)).containsExactly(4, 5, 6);

        grid.removeColumn();
        assertThat(grid.getHeight()).isEqualTo(2);
        assertThat(grid.getWidth()).isEqualTo(2);
        assertThat(grid.getRowValues(0)).containsExactly(0, 1);
        assertThat(grid.getRowValues(1)).containsExactly(4, 5);

        grid.removeRow();
        assertThat(grid.getHeight()).isEqualTo(1);
        assertThat(grid.getWidth()).isEqualTo(2);
        assertThat(grid.getRowValues(0)).containsExactly(0, 1);

        grid.removeRow();
        assertThat(grid.getHeight()).isEqualTo(0);
        assertThat(grid.getWidth()).isEqualTo(0);
    }

    @Test
    public void testImportExport() {
        List<List<Integer>> data = Arrays.asList(
                list(1, 2, 3),
                list(4, 5, 6)
        );

        grid.setGrid(data);
        assertThat(grid.getWidth()).isEqualTo(3);
        assertThat(grid.getHeight()).isEqualTo(2);
        assertThat(grid.exportGridValues()).containsExactly(data.get(0), data.get(1));
        assertThat(grid.getValue(1, 0)).isEqualTo(2);
        assertThat(grid.getValue(0, 1)).isEqualTo(4);
    }

}
