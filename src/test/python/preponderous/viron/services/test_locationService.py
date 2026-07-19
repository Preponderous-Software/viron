import pytest
from unittest.mock import patch, Mock
from src.main.python.preponderous.viron.services.locationService import (
    LocationService
)
from src.main.python.preponderous.viron.models.location import Location

service = LocationService("http://localhost", 9999)


def test_init():
    assert service.viron_host == "http://localhost"
    assert service.viron_port == 9999


def test_get_base_url():
    expected = "http://localhost:9999/api/v1/locations"
    assert service.get_base_url() == expected

@patch('requests.get')
def test_get_all_locations(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'locationId': 1, 'x': 10, 'y': 20},
        {'locationId': 2, 'x': 30, 'y': 40}
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    locations = service.get_all_locations()

    assert len(locations) == 2
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations", headers={})

@patch('requests.get')
def test_get_location_by_id(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = {'locationId': 1, 'x': 10, 'y': 20}
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    location = service.get_location_by_id(1)

    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/1", headers={})

@patch('requests.get')
def test_get_location_by_id_not_found(mock_get):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_get.return_value = mock_response

    with pytest.raises(Exception):
        service.get_location_by_id(1)

@patch('requests.get')
def test_get_locations_in_environment(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'locationId': 1, 'x': 10, 'y': 20},
        {'locationId': 2, 'x': 30, 'y': 40}
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    locations = service.get_locations_in_environment(1)

    assert len(locations) == 2
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/environment/1", headers={})

@patch('requests.get')
def test_get_locations_in_grid(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'locationId': 1, 'x': 10, 'y': 20},
        {'locationId': 2, 'x': 30, 'y': 40}
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    locations = service.get_locations_in_grid(1)

    assert len(locations) == 2
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/grid/1", headers={})

@patch('requests.get')
def test_get_unoccupied_locations_in_grid(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'locationId': 1, 'x': 10, 'y': 20},
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    locations = service.get_unoccupied_locations_in_grid(1)

    assert len(locations) == 1
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/grid/1/unoccupied", headers={})

@patch('requests.get')
def test_get_neighbors(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'locationId': 2, 'x': 11, 'y': 20},
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    neighbors = service.get_neighbors(1)

    assert len(neighbors) == 1
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/1/neighbors", headers={})

@patch('requests.get')
def test_get_neighbors_not_found(mock_get):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_get.return_value = mock_response

    with pytest.raises(Exception):
        service.get_neighbors(1)

@patch('requests.get')
def test_get_location_of_entity(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = {'locationId': 1, 'x': 10, 'y': 20}
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    location = service.get_location_of_entity(1)

    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/entity/1", headers={})

@patch('requests.put')
def test_add_entity_to_location(mock_put):
    mock_response = Mock()
    mock_response.raise_for_status = Mock()
    mock_put.return_value = mock_response

    service.add_entity_to_location(1, 1)

    mock_put.assert_called_with("http://localhost:9999/api/v1/locations/1/entity/1", headers={})

@patch('requests.delete')
def test_remove_entity_from_location(mock_delete):
    mock_response = Mock()
    mock_response.raise_for_status = Mock()
    mock_delete.return_value = mock_response

    service.remove_entity_from_location(1, 1)

    mock_delete.assert_called_with("http://localhost:9999/api/v1/locations/1/entity/1", headers={})

@patch('requests.delete')
def test_remove_entity_from_current_location(mock_delete):
    mock_response = Mock()
    mock_response.raise_for_status = Mock()
    mock_delete.return_value = mock_response

    service.remove_entity_from_current_location(1)

    mock_delete.assert_called_with("http://localhost:9999/api/v1/locations/entity/1", headers={})

@patch('requests.delete')
def test_remove_entity_not_found(mock_delete):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_delete.return_value = mock_response

    with pytest.raises(Exception):
        service.remove_entity_from_current_location(1)


@patch('requests.get')
def test_get_entity_ids_at_location(mock_get):
    mock_response = Mock()
    mock_response.status_code = 200
    mock_response.json.return_value = [1, 2, 3]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    entity_ids = service.get_entity_ids_at_location(1)

    assert entity_ids == [1, 2, 3]
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/1/entities", headers={})

@patch('requests.get')
def test_get_entity_ids_at_location_not_found(mock_get):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_get.return_value = mock_response

    with pytest.raises(Exception):
        service.get_entity_ids_at_location(1)

@patch('requests.get')
def test_is_location_occupied(mock_get):
    mock_response = Mock()
    mock_response.status_code = 200
    mock_response.json.return_value = True
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    occupied = service.is_location_occupied(1)

    assert occupied is True
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/1/occupied", headers={})

@patch('requests.get')
def test_is_location_occupied_not_found(mock_get):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_get.return_value = mock_response

    with pytest.raises(Exception):
        service.is_location_occupied(1)

@patch('requests.put')
def test_move_entity_to_location(mock_put):
    mock_response = Mock()
    mock_response.status_code = 204
    mock_response.raise_for_status = Mock()
    mock_put.return_value = mock_response

    service.move_entity_to_location(1, 2)

    mock_put.assert_called_with("http://localhost:9999/api/v1/locations/2/entity/1/move", headers={})

@patch('requests.put')
def test_move_entity_to_location_not_found(mock_put):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_put.return_value = mock_response

    with pytest.raises(Exception):
        service.move_entity_to_location(1, 2)

@patch('requests.put')
def test_move_entity_to_location_conflict(mock_put):
    mock_response = Mock()
    mock_response.status_code = 409
    mock_put.return_value = mock_response

    with pytest.raises(Exception):
        service.move_entity_to_location(1, 2)


def test_get_auth_headers_without_token():
    assert service.get_auth_headers() == {}


@patch('requests.get')
def test_get_all_locations_sends_bearer_token(mock_get):
    authed_service = LocationService("http://localhost", 9999, auth_token="test-token")
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    authed_service.get_all_locations()

    mock_get.assert_called_with(
        "http://localhost:9999/api/v1/locations",
        headers={"Authorization": "Bearer test-token"},
    )