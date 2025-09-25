from src.main.python.preponderous.viron.models.grid import Grid
import pytest

@pytest.fixture
def grid():
    return Grid(1, 10, 20)

def test_grid_initialization(grid):
    assert grid.get_grid_id() == 1
    assert grid.get_rows() == 10
    assert grid.get_columns() == 20

def test_get_set_grid_id(grid):
    grid.set_grid_id(2)
    assert grid.get_grid_id() == 2

def test_get_set_rows(grid):
    grid.set_rows(15)
    assert grid.get_rows() == 15

def test_get_set_columns(grid):
    grid.set_columns(25)
    assert grid.get_columns() == 25

def test_str_representation(grid):
    assert str(grid) == "Grid{grid_id=1, rows=10, columns=20}"