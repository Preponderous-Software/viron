// Copyright (c) 2024 Preponderous Software
// MIT License

package preponderous.viron.models;

import lombok.Data;

@Data
public class Environment {
    private int environmentId;
    private String name;
    private String creationDate;

    public Environment(int environmentId, String name, String creationDate) {
        this.environmentId = environmentId;
        this.name = name;
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "environmentId=" + environmentId +
                ", name='" + name + '\'' +
                ", creationDate='" + creationDate + '\'' +
                '}';
    }
}
