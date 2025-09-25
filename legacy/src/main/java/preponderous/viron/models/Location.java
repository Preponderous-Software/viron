// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import lombok.Data;

@Data
public class Location {
    private int locationId;
    private int x;
    private int y;

    public Location(int locationId, int x, int y) {
        this.locationId = locationId;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Location{" +
                "locationId=" + locationId +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
