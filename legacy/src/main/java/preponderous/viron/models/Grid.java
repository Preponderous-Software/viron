// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import lombok.Data;

@Data
public class Grid {
    private int gridId;
    private int rows;
    private int columns;

    public Grid(int gridId, int rows, int columns) {
        this.gridId = gridId;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Grid{" +
                "gridId=" + gridId +
                ", rows=" + rows +
                ", columns=" + columns +
                '}';
    }
}
