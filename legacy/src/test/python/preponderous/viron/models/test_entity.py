# Copyright (c) 2024 Preponderous Software
# MIT License

import pytest

from src.main.python.preponderous.viron.models.entity import Entity


@pytest.fixture
def entity():
    return Entity(1, "Test Entity", "2024-01-01")

def test_getEntityId(entity):
    assert entity.getEntityId() == 1

def test_setEntityId(entity):
    entity.setEntityId(2)
    assert entity.getEntityId() == 2

def test_getName(entity):
    assert entity.getName() == "Test Entity"

def test_setName(entity):
    entity.setName("New Name")
    assert entity.getName() == "New Name"

def test_getCreationDate(entity):
    assert entity.getCreationDate() == "2024-01-01"

def test_setCreationDate(entity):
    entity.setCreationDate("2024-02-01")
    assert entity.getCreationDate() == "2024-02-01"

def test_str(entity):
    assert str(entity) == "Entity{entityId=1, name=Test Entity, creationDate=2024-01-01}"

def test_setEntityId_invalid_type(entity):
    with pytest.raises(TypeError):
        entity.setEntityId("invalid")

def test_setName_invalid_type(entity):
    with pytest.raises(TypeError):
        entity.setName(123)

def test_setCreationDate_invalid_format(entity):
    with pytest.raises(ValueError):
        entity.setCreationDate("invalid-date")