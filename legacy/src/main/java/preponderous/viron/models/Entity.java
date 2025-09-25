// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import lombok.Data;

@Data
public class Entity {
    private int entityId;
    private String name;
    private String creationDate;

    public Entity(int entityId, String name, String creationDate) {
        this.entityId = entityId;
        this.name = name;
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "entityId=" + entityId +
                ", name='" + name + '\'' +
                ", creationDate='" + creationDate + '\'' +
                '}';
    }
}
