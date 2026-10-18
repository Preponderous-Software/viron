# Copyright (c) 2024 Preponderous Software
# MIT License

class Placement:
    def __init__(self, entityId: int, environmentId: int, gridId: int, locationId: int, x: int, y: int):
        self.entityId = entityId
        self.environmentId = environmentId
        self.gridId = gridId
        self.locationId = locationId
        self.x = x
        self.y = y

    def get_entity_id(self) -> int:
        return self.entityId

    def get_environment_id(self) -> int:
        return self.environmentId

    def get_grid_id(self) -> int:
        return self.gridId

    def get_location_id(self) -> int:
        return self.locationId

    def get_x(self) -> int:
        return self.x

    def get_y(self) -> int:
        return self.y

    def __str__(self) -> str:
        return (f"Placement{{entity_id={self.entityId}, environment_id={self.environmentId}, "
                f"grid_id={self.gridId}, location_id={self.locationId}, x={self.x}, y={self.y}}}")
