import pytest
from src.main.python.preponderous.viron.models.location import Location

@pytest.fixture
def location():
    return Location(locationId=1, x=10, y=20)

def test_get_location_id(location):
    assert location.get_location_id() == 1

def test_set_location_id(location):
    location.set_location_id(2)
    assert location.get_location_id() == 2

def test_get_x(location):
    assert location.get_x() == 10

def test_set_x(location):
    location.set_x(15)
    assert location.get_x() == 15

def test_get_y(location):
    assert location.get_y() == 20

def test_set_y(location):
    location.set_y(25)
    assert location.get_y() == 25

def test_str(location):
    assert str(location) == "Location{location_id=1, x=10, y=20}"
