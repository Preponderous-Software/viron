import pytest
import requests
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

@patch('requests.get')
def test_get_placements(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'entityId': 1, 'environmentId': 7, 'gridId': 11, 'locationId': 110, 'x': 5, 'y': 6},
        {'entityId': 2, 'environmentId': 8, 'gridId': 20, 'locationId': 200, 'x': 13, 'y': 14}
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    placements = service.get_placements()

    assert [p.get_entity_id() for p in placements] == [1, 2]
    assert placements[0].get_environment_id() == 7
    assert placements[0].get_grid_id() == 11
    assert placements[0].get_location_id() == 110
    assert (placements[0].get_x(), placements[0].get_y()) == (5, 6)
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/placements", params=None, headers={})

@patch('requests.get')
def test_get_placements_in_environment(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    assert service.get_placements(7) == []
    mock_get.assert_called_with("http://localhost:9999/api/v1/locations/placements",
                                params={"environmentId": 7}, headers={})

@patch('requests.put')
def test_add_entity_to_location(mock_put):
    mock_response = Mock()
    mock_response.raise_for_status = Mock()
    mock_put.return_value = mock_response

    service.add_entity_to_location(1, 1)

    mock_put.assert_called_with("http://localhost:9999/api/v1/locations/1/entity/1", headers={})

@patch('requests.put')
def test_add_entity_to_location_not_found(mock_put):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_put.return_value = mock_response

    with pytest.raises(Exception):
        service.add_entity_to_location(1, 1)

@patch('requests.put')
def test_add_entity_to_location_conflict(mock_put):
    mock_response = Mock()
    mock_response.status_code = 409
    mock_put.return_value = mock_response

    with pytest.raises(Exception):
        service.add_entity_to_location(1, 1)

@patch('requests.delete')
def test_remove_entity_from_location(mock_delete):
    mock_response = Mock()
    mock_response.raise_for_status = Mock()
    mock_delete.return_value = mock_response

    service.remove_entity_from_location(1, 1)

    mock_delete.assert_called_with("http://localhost:9999/api/v1/locations/1/entity/1", headers={})

@patch('requests.delete')
def test_remove_entity_from_location_not_there(mock_delete):
    mock_response = Mock()
    mock_response.status_code = 404
    mock_delete.return_value = mock_response

    with pytest.raises(Exception, match="No location 1 holding entity 2"):
        service.remove_entity_from_location(2, 1)

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


# Failure paths. Unlike GridService and EnvironmentService, LocationService checks a 404 (and, on
# the two placement writes, a 409) itself before calling raise_for_status, and raises a plain
# Exception with its own message. A caller catching requests.exceptions.HTTPError therefore misses
# those answers. Every other error status, and every transport error, propagates from requests
# unwrapped. None of the methods guards an empty body.
def _status_response(status):
    response = Mock()
    response.status_code = status
    return response


def _error_response(status):
    response = _status_response(status)
    response.raise_for_status.side_effect = requests.exceptions.HTTPError(f"{status} Error")
    return response


@pytest.mark.parametrize("call, message", [
    (lambda: service.get_location_by_id(99), "Location not found with id: 99"),
    (lambda: service.get_neighbors(99), "Location not found with id: 99"),
    (lambda: service.get_location_of_entity(99), "Location not found for entity: 99"),
    (lambda: service.get_entity_ids_at_location(99), "Location not found with id: 99"),
    (lambda: service.is_location_occupied(99), "Location not found with id: 99"),
], ids=["by_id", "neighbors", "of_entity", "entity_ids", "occupied"])
@patch('requests.get')
def test_lookup_not_found_raises_plain_exception_without_reading_body(mock_get, call, message):
    mock_get.return_value = _status_response(404)

    with pytest.raises(Exception) as exc_info:
        call()
    assert exc_info.type is Exception
    assert str(exc_info.value) == message
    mock_get.return_value.raise_for_status.assert_not_called()
    mock_get.return_value.json.assert_not_called()


@pytest.mark.parametrize("call, method, message", [
    (lambda: service.add_entity_to_location(5, 7), 'put', "Location or entity not found"),
    (lambda: service.remove_entity_from_current_location(5), 'delete', "Entity not found"),
    (lambda: service.move_entity_to_location(5, 7), 'put', "Entity or location not found"),
], ids=["add", "remove_current", "move"])
def test_write_not_found_raises_plain_exception(call, method, message):
    with patch(f'requests.{method}') as mock_request:
        mock_request.return_value = _status_response(404)

        with pytest.raises(Exception) as exc_info:
            call()
    assert exc_info.type is Exception
    assert str(exc_info.value) == message
    mock_request.return_value.raise_for_status.assert_not_called()


@pytest.mark.parametrize("call, message", [
    (lambda: service.add_entity_to_location(5, 7), "Entity 5 is already placed at another location"),
    (lambda: service.move_entity_to_location(5, 7), "Target location 7 is already occupied"),
], ids=["add", "move"])
@patch('requests.put')
def test_placement_conflict_raises_plain_exception(mock_put, call, message):
    mock_put.return_value = _status_response(409)

    with pytest.raises(Exception) as exc_info:
        call()
    assert exc_info.type is Exception
    assert str(exc_info.value) == message
    mock_put.return_value.raise_for_status.assert_not_called()


# The lists that have no 404 of their own (the server answers an unknown environment or grid with
# an empty list) pass every error status through raise_for_status. A 400 is what the server's
# @Min(1) validation answers for a zero id.
@pytest.mark.parametrize("call", [
    lambda: service.get_all_locations(),
    lambda: service.get_locations_in_environment(0),
    lambda: service.get_locations_in_grid(0),
    lambda: service.get_unoccupied_locations_in_grid(0),
    lambda: service.get_placements(0),
], ids=["all", "in_environment", "in_grid", "unoccupied", "placements"])
@patch('requests.get')
def test_list_error_raises_http_error_without_reading_body(mock_get, call):
    mock_get.return_value = _error_response(400)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        call()
    assert exc_info.type is requests.exceptions.HTTPError
    assert "400" in str(exc_info.value)
    mock_get.return_value.json.assert_not_called()


# environment_id is compared against None rather than tested for truth, so 0 is sent as a query
# parameter (and rejected by the server) instead of being dropped.
@patch('requests.get')
def test_get_placements_sends_zero_environment_id(mock_get):
    mock_get.return_value.json.return_value = []

    service.get_placements(0)

    mock_get.assert_called_once_with("http://localhost:9999/api/v1/locations/placements",
                                     params={"environmentId": 0}, headers={})


# The move's 400s (target in another grid, or not adjacent) and the 503 that a lock wait past its
# bound is answered with are not mapped by the client.
@pytest.mark.parametrize("status", [400, 503])
@patch('requests.put')
def test_move_entity_to_location_other_error_raises_http_error_unwrapped(mock_put, status):
    mock_put.return_value = _error_response(status)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        service.move_entity_to_location(5, 7)
    assert exc_info.type is requests.exceptions.HTTPError
    assert str(status) in str(exc_info.value)


@patch('requests.put')
def test_add_entity_to_location_lock_wait_raises_http_error_unwrapped(mock_put):
    mock_put.return_value = _error_response(503)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        service.add_entity_to_location(5, 7)
    assert exc_info.type is requests.exceptions.HTTPError
    assert "503" in str(exc_info.value)


@pytest.mark.parametrize("call, method", [
    (lambda: service.get_location_by_id(1), 'get'),
    (lambda: service.add_entity_to_location(5, 7), 'put'),
    (lambda: service.remove_entity_from_location(5, 7), 'delete'),
    (lambda: service.move_entity_to_location(5, 7), 'put'),
], ids=["by_id", "add", "remove", "move"])
def test_network_error_propagates_unwrapped(call, method):
    with patch(f'requests.{method}') as mock_request:
        mock_request.side_effect = requests.exceptions.ConnectionError("connection refused")

        with pytest.raises(requests.exceptions.ConnectionError) as exc_info:
            call()
    assert str(exc_info.value) == "connection refused"


AUTH_HEADERS = {"Authorization": "Bearer test-token"}


def _authed_service():
    return LocationService("http://localhost", 9999, auth_token="test-token")


@pytest.mark.parametrize("call, method, url", [
    (lambda s: s.add_entity_to_location(5, 7), 'put',
     "http://localhost:9999/api/v1/locations/7/entity/5"),
    (lambda s: s.remove_entity_from_location(5, 7), 'delete',
     "http://localhost:9999/api/v1/locations/7/entity/5"),
    (lambda s: s.remove_entity_from_current_location(5), 'delete',
     "http://localhost:9999/api/v1/locations/entity/5"),
    (lambda s: s.move_entity_to_location(5, 7), 'put',
     "http://localhost:9999/api/v1/locations/7/entity/5/move"),
], ids=["add", "remove", "remove_current", "move"])
def test_write_sends_bearer_token(call, method, url):
    with patch(f'requests.{method}') as mock_request:
        mock_request.return_value.status_code = 204

        call(_authed_service())

    mock_request.assert_called_once_with(url, headers=AUTH_HEADERS)


# The two scalar reads hand back the decoded body as it is: is_location_occupied does not coerce
# it to bool, and neither maps a null body to a default.
@patch('requests.get')
def test_scalar_reads_return_body_unconverted(mock_get):
    mock_get.return_value.status_code = 200
    mock_get.return_value.json.return_value = None

    assert service.get_entity_ids_at_location(1) is None
    assert service.is_location_occupied(1) is None


@pytest.mark.parametrize("call", [
    lambda: service.get_all_locations(),
    lambda: service.get_location_by_id(1),
    lambda: service.get_locations_in_environment(1),
    lambda: service.get_locations_in_grid(1),
    lambda: service.get_unoccupied_locations_in_grid(1),
    lambda: service.get_neighbors(1),
    lambda: service.get_location_of_entity(1),
    lambda: service.get_placements(),
], ids=["all", "by_id", "in_environment", "in_grid", "unoccupied", "neighbors", "of_entity", "placements"])
@patch('requests.get')
def test_empty_body_raises_type_error(mock_get, call):
    mock_get.return_value.status_code = 200
    mock_get.return_value.json.return_value = None

    with pytest.raises(TypeError):
        call()