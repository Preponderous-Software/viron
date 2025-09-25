import pytest
from unittest.mock import patch, Mock
from src.main.python.preponderous.viron.services.entityService import EntityService
from src.main.python.preponderous.viron.models.entity import Entity

# Test data
MOCK_ENTITY_DATA = {
    'entityId': 1,
    'name': 'Entity1',
    'creationDate': '2024-01-01'
}

MOCK_ENTITIES_DATA = [
    {'entityId': 1, 'name': 'Entity1', 'creationDate': '2024-01-01'},
    {'entityId': 2, 'name': 'Entity2', 'creationDate': '2024-01-01'}
]

service = EntityService("http://localhost", 9999)


def test_init():
    assert service.viron_host == "http://localhost"
    assert service.viron_port == 9999


def test_get_base_url():
    expected = "http://localhost:9999/api/v1/entities"
    assert service.get_base_url() == expected


@patch('requests.get')
def test_get_all_entities_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_all_entities()
    assert len(entities) == 2
    assert isinstance(entities[0], Entity)
    mock_get.assert_called_once_with(service.get_base_url())


@patch('requests.get')
def test_get_all_entities_empty(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_get.return_value = mock_response

    entities = service.get_all_entities()
    assert len(entities) == 0


@patch('requests.get')
def test_get_entity_by_id_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITY_DATA
    mock_get.return_value = mock_response

    entity = service.get_entity_by_id(1)
    assert isinstance(entity, Entity)
    assert entity.getEntityId() == 1
    mock_get.assert_called_once_with(f"{service.get_base_url()}/1")


@patch('requests.get')
def test_get_entities_in_environment_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_in_environment(1)
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/environment/1")


@patch('requests.get')
def test_get_entities_in_grid_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_in_grid(1)
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/grid/1")


@patch('requests.get')
def test_get_entities_in_location_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_in_location(1)
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/location/1")


@patch('requests.get')
def test_get_entities_not_in_any_location_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_not_in_any_location()
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/unassigned")


@patch('requests.post')
def test_create_entity_success(mock_post):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITY_DATA
    mock_post.return_value = mock_response

    entity = service.create_entity("Entity1")
    assert isinstance(entity, Entity)
    assert entity.name == "Entity1"
    mock_post.assert_called_once_with(f"{service.get_base_url()}/Entity1")


@patch('requests.delete')
def test_delete_entity_success(mock_delete):
    mock_response = Mock()
    mock_delete.return_value = mock_response

    result = service.delete_entity(1)
    assert result is True
    mock_delete.assert_called_once_with(f"{service.get_base_url()}/1")


@patch('requests.patch')
def test_update_entity_name_success(mock_patch):
    mock_response = Mock()
    mock_patch.return_value = mock_response

    result = service.update_entity_name(1, "NewName")
    assert result is True
    mock_patch.assert_called_once_with(f"{service.get_base_url()}/1/name/NewName")


# Error cases
@patch('requests.get')
def test_get_all_entities_error(mock_get):
    mock_get.side_effect = Exception("Network error")

    with pytest.raises(Exception) as exc_info:
        service.get_all_entities()
    assert "Network error" in str(exc_info.value)


@patch('requests.post')
def test_create_entity_null_response(mock_post):
    mock_response = Mock()
    mock_response.json.return_value = None
    mock_post.return_value = mock_response

    with pytest.raises(Exception) as exc_info:
        service.create_entity("Entity1")
    assert "Created entity response was null" in str(exc_info.value)

