import pytest

from src.main.python.preponderous.viron.models.environment import Environment

@pytest.fixture
def environment():
    return Environment(1, "Test Environment", "2024-01-01")

def test_environment_initialization(environment):
    assert environment.getEnvironmentId() == 1
    assert environment.getName() == "Test Environment"
    assert environment.getCreationDate() == "2024-01-01"

def test_set_environment_id(environment):
    environment.setEnvironmentId(2)
    assert environment.getEnvironmentId() == 2

def test_set_environment_id_invalid_type(environment):
    with pytest.raises(TypeError):
        environment.setEnvironmentId("invalid")

def test_set_name(environment):
    environment.setName("New Environment Name")
    assert environment.getName() == "New Environment Name"

def test_set_name_invalid_type(environment):
    with pytest.raises(TypeError):
        environment.setName(123)

def test_set_creation_date(environment):
    environment.setCreationDate("2025-12-31")
    assert environment.getCreationDate() == "2025-12-31"

# def test_set_creation_date_invalid_format(environment):
#     with pytest.raises(ValueError):
#         environment.setCreationDate("31-12-2025")

def test_environment_str(environment):
    assert str(environment) == "Environment{environmentId=1, name=Test Environment, creationDate=2024-01-01}"