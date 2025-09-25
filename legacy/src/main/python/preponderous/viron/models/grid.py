# Copyright (c) 2024 Preponderous Software
# MIT License

class Grid:
    def __init__(self, gridId: int, rows: int, columns: int):
        self.grid_id = gridId
        self.rows = rows
        self.columns = columns

    def get_grid_id(self) -> int:
        return self.grid_id

    def set_grid_id(self, grid_id: int):
        self.grid_id = grid_id

    def get_rows(self) -> int:
        return self.rows

    def set_rows(self, rows: int):
        self.rows = rows

    def get_columns(self) -> int:
        return self.columns

    def set_columns(self, columns: int):
        self.columns = columns

    def __str__(self) -> str:
        return f"Grid{{grid_id={self.grid_id}, rows={self.rows}, columns={self.columns}}}"