import pytest
from src.main.python.preponderous.viron.models.placement import Placement

@pytest.fixture
def placement():
    return Placement(entityId=1, environmentId=7, gridId=11, locationId=110, x=5, y=6)

def test_get_entity_id(placement):
    assert placement.get_entity_id() == 1

def test_get_environment_id(placement):
    assert placement.get_environment_id() == 7

def test_get_grid_id(placement):
    assert placement.get_grid_id() == 11

def test_get_location_id(placement):
    assert placement.get_location_id() == 110

def test_get_x(placement):
    assert placement.get_x() == 5

def test_get_y(placement):
    assert placement.get_y() == 6

def test_str(placement):
    assert str(placement) == "Placement{entity_id=1, environment_id=7, grid_id=11, location_id=110, x=5, y=6}"
