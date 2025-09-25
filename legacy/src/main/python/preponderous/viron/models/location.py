# Copyright (c) 2024 Preponderous Software
# MIT License

class Location:
    def __init__(self, locationId: int, x: int, y: int):
        self.locationId = locationId
        self.x = x
        self.y = y

    def get_location_id(self) -> int:
        return self.locationId

    def set_location_id(self, location_id: int):
        self.locationId = location_id

    def get_x(self) -> int:
        return self.x

    def set_x(self, x: int):
        self.x = x

    def get_y(self) -> int:
        return self.y

    def set_y(self, y: int):
        self.y = y

    def __str__(self) -> str:
        return f"Location{{location_id={self.locationId}, x={self.x}, y={self.y}}}"