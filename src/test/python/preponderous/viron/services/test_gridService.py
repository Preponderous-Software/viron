import pytest
from unittest.mock import patch, Mock
import requests
from requests.exceptions import HTTPError
from src.main.python.preponderous.viron.services.gridService import GridService
from src.main.python.preponderous.viron.models.grid import Grid

BASE_URL = "http://localhost:9999"
API_PATH = "/api/v1/grids"

service = GridService("http://localhost", 9999)


@pytest.fixture
def mock_response():
    response = Mock()
    response.raise_for_status = Mock()
    return response

def test_init():
    assert service.viron_host == "http://localhost"
    assert service.viron_port == 9999


def test_get_base_url():
    expected = "http://localhost:9999/api/v1/grids"
    assert service.get_base_url() == expected

class TestGetAllGrids:
    @patch('requests.get')
    def test_success(self, mock_get, mock_response):
        mock_response.json.return_value = [
            {'gridId': 1, 'rows': 10, 'columns': 20},
            {'gridId': 2, 'rows': 15, 'columns': 25}
        ]
        mock_get.return_value = mock_response

        grids = service.get_all_grids()

        assert len(grids) == 2
        assert isinstance(grids[0], Grid)
        assert grids[0].get_grid_id() == 1
        assert grids[0].get_rows() == 10
        assert grids[0].get_columns() == 20
        assert grids[1].get_grid_id() == 2
        assert grids[1].get_rows() == 15
        assert grids[1].get_columns() == 25
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}", headers={})

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_all_grids()
        assert str(exc_info.value) == "Test error"

class TestGetGridById:
    @patch('requests.get')
    def test_success(self, mock_get, mock_response):
        mock_response.json.return_value = {'gridId': 1, 'rows': 10, 'columns': 20, 'name': 'battlefield'}
        mock_get.return_value = mock_response

        grid = service.get_grid_by_id(1)

        assert isinstance(grid, Grid)
        assert grid.get_grid_id() == 1
        assert grid.get_rows() == 10
        assert grid.get_columns() == 20
        assert grid.get_name() == 'battlefield'
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/1", headers={})

    @patch('requests.get')
    def test_not_found(self, mock_get):
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = Exception()
        mock_get.return_value = mock_response

        with pytest.raises(Exception) as exc_info:
            grid = service.get_grid_by_id(1)
            assert grid is None

        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/1", headers={})

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_grid_by_id(1)
        assert str(exc_info.value) == "Test error"

class TestGetGridsInEnvironment:
    @patch('requests.get')
    def test_success(self, mock_get, mock_response):
        mock_response.json.return_value = [
            {'gridId': 1, 'rows': 10, 'columns': 20},
            {'gridId': 2, 'rows': 15, 'columns': 25}
        ]
        mock_get.return_value = mock_response

        grids = service.get_grids_in_environment(1)

        assert len(grids) == 2
        assert isinstance(grids[0], Grid)
        assert grids[0].get_grid_id() == 1
        assert grids[1].get_grid_id() == 2
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/environment/1", headers={})

    @patch('requests.get')
    def test_empty_list(self, mock_get, mock_response):
        mock_response.json.return_value = []
        mock_get.return_value = mock_response

        grids = service.get_grids_in_environment(1)

        assert len(grids) == 0
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/environment/1", headers={})

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_grids_in_environment(1)
        assert str(exc_info.value) == "Test error"

class TestAuthToken:
    def test_get_auth_headers_without_token(self):
        assert service.get_auth_headers() == {}

    @patch('requests.get')
    def test_get_all_grids_sends_bearer_token(self, mock_get, mock_response):
        authed_service = GridService("http://localhost", 9999, auth_token="test-token")
        mock_response.json.return_value = []
        mock_get.return_value = mock_response

        authed_service.get_all_grids()

        mock_get.assert_called_once_with(
            f"{BASE_URL}{API_PATH}",
            headers={"Authorization": "Bearer test-token"},
        )

class TestGetGridOfEntity:
    @patch('requests.get')
    def test_success(self, mock_get, mock_response):
        mock_response.json.return_value = {'gridId': 1, 'rows': 10, 'columns': 20}
        mock_get.return_value = mock_response

        grid = service.get_grid_of_entity(1)

        assert isinstance(grid, Grid)
        assert grid.get_grid_id() == 1
        assert grid.get_rows() == 10
        assert grid.get_columns() == 20
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/entity/1", headers={})

    @patch('requests.get')
    def test_not_found(self, mock_get):
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = HTTPError(response=Mock(status_code=404))
        mock_get.return_value = mock_response

        with pytest.raises(Exception) as exc_info:
            grid = service.get_grid_of_entity(1)
            assert grid is None

        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/entity/1", headers={})

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_grid_of_entity(1)
        assert str(exc_info.value) == "Test error"


class TestUpdateGridName:
    @patch('requests.patch')
    def test_success(self, mock_patch):
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.raise_for_status = Mock()
        mock_patch.return_value = mock_response

        result = service.update_grid_name(1, "NewName")

        assert result is True
        mock_patch.assert_called_once_with(
            f"{BASE_URL}{API_PATH}/1/name",
            json={"name": "NewName"},
            headers={},
        )

    @patch('requests.patch')
    def test_error(self, mock_patch):
        mock_patch.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.update_grid_name(1, "NewName")
        assert str(exc_info.value) == "Test error"


# Failure paths. Unlike EntityService.update_entity_name, every GridService method lets the
# error from requests or from raise_for_status propagate as it is, so a caller can catch
# HTTPError on the write as well as on the reads. None of the methods guards an empty body.
def _error_response(status):
    response = Mock()
    response.raise_for_status.side_effect = HTTPError(f"{status} Error")
    return response


class TestFailurePaths:
    @patch('requests.get')
    def test_get_all_grids_server_error_raises_http_error(self, mock_get):
        mock_get.return_value = _error_response(503)

        with pytest.raises(HTTPError) as exc_info:
            service.get_all_grids()
        assert "503" in str(exc_info.value)
        mock_get.return_value.json.assert_not_called()

    @patch('requests.get')
    def test_get_grid_by_id_not_found_raises_http_error(self, mock_get):
        mock_get.return_value = _error_response(404)

        with pytest.raises(HTTPError) as exc_info:
            service.get_grid_by_id(99)
        assert "404" in str(exc_info.value)
        mock_get.return_value.json.assert_not_called()

    @patch('requests.get')
    def test_get_grids_in_environment_not_found_raises_http_error(self, mock_get):
        mock_get.return_value = _error_response(404)

        with pytest.raises(HTTPError) as exc_info:
            service.get_grids_in_environment(99)
        assert "404" in str(exc_info.value)
        mock_get.return_value.json.assert_not_called()

    @patch('requests.get')
    def test_get_grid_of_entity_not_found_raises_http_error(self, mock_get):
        mock_get.return_value = _error_response(404)

        with pytest.raises(HTTPError) as exc_info:
            service.get_grid_of_entity(99)
        assert "404" in str(exc_info.value)
        mock_get.return_value.json.assert_not_called()

    @patch('requests.patch')
    def test_update_grid_name_not_found_raises_http_error_unwrapped(self, mock_patch):
        mock_patch.return_value = _error_response(404)

        with pytest.raises(HTTPError) as exc_info:
            service.update_grid_name(99, "NewName")
        assert exc_info.type is HTTPError
        assert "404" in str(exc_info.value)

    @patch('requests.patch')
    def test_update_grid_name_rejected_raises_http_error_unwrapped(self, mock_patch):
        mock_patch.return_value = _error_response(400)

        with pytest.raises(HTTPError) as exc_info:
            service.update_grid_name(1, "x" * 256)
        assert exc_info.type is HTTPError
        assert "400" in str(exc_info.value)

    @patch('requests.patch')
    def test_update_grid_name_network_error_propagates_unwrapped(self, mock_patch):
        mock_patch.side_effect = requests.exceptions.ConnectionError("connection refused")

        with pytest.raises(requests.exceptions.ConnectionError) as exc_info:
            service.update_grid_name(1, "NewName")
        assert str(exc_info.value) == "connection refused"

    @patch('requests.patch')
    def test_update_grid_name_other_success_status_returns_false(self, mock_patch):
        mock_response = Mock()
        mock_response.status_code = 204
        mock_patch.return_value = mock_response

        assert service.update_grid_name(1, "NewName") is False

    @patch('requests.patch')
    def test_update_grid_name_sends_bearer_token(self, mock_patch):
        authed_service = GridService("http://localhost", 9999, auth_token="test-token")
        mock_response = Mock()
        mock_response.status_code = 200
        mock_patch.return_value = mock_response

        authed_service.update_grid_name(1, "NewName")

        mock_patch.assert_called_once_with(
            f"{BASE_URL}{API_PATH}/1/name",
            json={"name": "NewName"},
            headers={"Authorization": "Bearer test-token"},
        )

    # Current behaviour, which differs from EntityService: an empty (null) body is not mapped
    # to None or [], so the model constructor or the list comprehension raises TypeError.
    @patch('requests.get')
    def test_get_grid_by_id_empty_body_raises_type_error(self, mock_get):
        mock_response = Mock()
        mock_response.json.return_value = None
        mock_get.return_value = mock_response

        with pytest.raises(TypeError):
            service.get_grid_by_id(1)

    @patch('requests.get')
    def test_get_all_grids_empty_body_raises_type_error(self, mock_get):
        mock_response = Mock()
        mock_response.json.return_value = None
        mock_get.return_value = mock_response

        with pytest.raises(TypeError):
            service.get_all_grids()