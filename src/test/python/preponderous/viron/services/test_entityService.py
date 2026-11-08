import pytest
from unittest.mock import patch, Mock
from requests.exceptions import ConnectionError, HTTPError
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
    mock_get.assert_called_once_with(service.get_base_url(), headers={})


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
    mock_get.assert_called_once_with(f"{service.get_base_url()}/1", headers={})


@patch('requests.get')
def test_get_entities_in_environment_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_in_environment(1)
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/environment/1", headers={})


@patch('requests.get')
def test_get_entities_in_grid_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_in_grid(1)
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/grid/1", headers={})


@patch('requests.get')
def test_get_entities_in_location_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_in_location(1)
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/location/1", headers={})


@patch('requests.get')
def test_get_entities_not_in_any_location_success(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITIES_DATA
    mock_get.return_value = mock_response

    entities = service.get_entities_not_in_any_location()
    assert len(entities) == 2
    mock_get.assert_called_once_with(f"{service.get_base_url()}/unassigned", headers={})


@patch('requests.post')
def test_create_entity_success(mock_post):
    mock_response = Mock()
    mock_response.json.return_value = MOCK_ENTITY_DATA
    mock_post.return_value = mock_response

    entity = service.create_entity("Entity1")
    assert isinstance(entity, Entity)
    assert entity.name == "Entity1"
    mock_post.assert_called_once_with(service.get_base_url(), json={"name": "Entity1"}, headers={})


@patch('requests.delete')
def test_delete_entity_success(mock_delete):
    mock_response = Mock()
    mock_delete.return_value = mock_response

    result = service.delete_entity(1)
    assert result is True
    mock_delete.assert_called_once_with(f"{service.get_base_url()}/1", headers={})


@patch('requests.patch')
def test_update_entity_name_success(mock_patch):
    mock_response = Mock()
    mock_patch.return_value = mock_response

    result = service.update_entity_name(1, "NewName")
    assert result is True
    mock_patch.assert_called_once_with(f"{service.get_base_url()}/1/name", json={"name": "NewName"}, headers={})


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


def test_get_auth_headers_without_token():
    assert service.get_auth_headers() == {}


@patch('requests.get')
def test_get_all_entities_sends_bearer_token(mock_get):
    authed_service = EntityService("http://localhost", 9999, auth_token="test-token")
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_get.return_value = mock_response

    authed_service.get_all_entities()

    mock_get.assert_called_once_with(
        authed_service.get_base_url(),
        headers={"Authorization": "Bearer test-token"},
    )


# Failure paths. The reads and create_entity let the HTTPError from raise_for_status propagate
# as it is; delete_entity and update_entity_name catch it and re-raise a bare Exception that
# carries a fixed message and the original error as its second argument.
def _error_response(status):
    response = Mock()
    response.raise_for_status.side_effect = HTTPError(f"{status} Client Error")
    return response


@patch('requests.get')
def test_get_entity_by_id_not_found_raises_http_error(mock_get):
    mock_get.return_value = _error_response(404)

    with pytest.raises(HTTPError) as exc_info:
        service.get_entity_by_id(99)
    assert "404" in str(exc_info.value)
    mock_get.return_value.json.assert_not_called()


@patch('requests.get')
def test_get_entity_by_id_empty_body_returns_none(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = None
    mock_get.return_value = mock_response

    assert service.get_entity_by_id(1) is None


@patch('requests.get')
def test_get_entities_in_environment_empty_body_returns_empty_list(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = None
    mock_get.return_value = mock_response

    assert service.get_entities_in_environment(1) == []


@patch('requests.post')
def test_create_entity_rejected_raises_http_error(mock_post):
    mock_post.return_value = _error_response(400)

    with pytest.raises(HTTPError) as exc_info:
        service.create_entity("x" * 256)
    assert "400" in str(exc_info.value)
    mock_post.return_value.json.assert_not_called()


@patch('requests.delete')
def test_delete_entity_not_found_wraps_the_http_error(mock_delete):
    mock_delete.return_value = _error_response(404)

    with pytest.raises(Exception) as exc_info:
        service.delete_entity(99)
    assert exc_info.type is Exception
    assert exc_info.value.args[0] == "Error deleting entity"
    assert isinstance(exc_info.value.args[1], HTTPError)
    assert "404" in str(exc_info.value.args[1])


@patch('requests.delete')
def test_delete_entity_network_error_is_wrapped(mock_delete):
    mock_delete.side_effect = ConnectionError("connection refused")

    with pytest.raises(Exception) as exc_info:
        service.delete_entity(1)
    assert exc_info.type is Exception
    assert exc_info.value.args[0] == "Error deleting entity"
    assert isinstance(exc_info.value.args[1], ConnectionError)


@patch('requests.patch')
def test_update_entity_name_not_found_wraps_the_http_error(mock_patch):
    mock_patch.return_value = _error_response(404)

    with pytest.raises(Exception) as exc_info:
        service.update_entity_name(99, "NewName")
    assert exc_info.type is Exception
    assert exc_info.value.args[0] == "Error updating entity name"
    assert isinstance(exc_info.value.args[1], HTTPError)
    assert "404" in str(exc_info.value.args[1])


@patch('requests.patch')
def test_update_entity_name_rejected_wraps_the_http_error(mock_patch):
    mock_patch.return_value = _error_response(400)

    with pytest.raises(Exception) as exc_info:
        service.update_entity_name(1, "x" * 256)
    assert exc_info.type is Exception
    assert exc_info.value.args[0] == "Error updating entity name"
    assert "400" in str(exc_info.value.args[1])

