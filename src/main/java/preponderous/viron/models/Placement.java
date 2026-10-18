// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import lombok.Data;

/**
 * Where one placed entity stands: the entity, the environment and grid holding its location, and
 * the location itself with its coordinates.
 */
@Data
public class Placement {
    private int entityId;
    private int environmentId;
    private int gridId;
    private int locationId;
    private int x;
    private int y;

    public Placement(int entityId, int environmentId, int gridId, int locationId, int x, int y) {
        this.entityId = entityId;
        this.environmentId = environmentId;
        this.gridId = gridId;
        this.locationId = locationId;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Placement{" +
                "entityId=" + entityId +
                ", environmentId=" + environmentId +
                ", gridId=" + gridId +
                ", locationId=" + locationId +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
