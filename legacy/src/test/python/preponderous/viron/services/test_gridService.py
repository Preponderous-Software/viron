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
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}")

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_all_grids()
        assert str(exc_info.value) == "Test error"

class TestGetGridById:
    @patch('requests.get')
    def test_success(self, mock_get, mock_response):
        mock_response.json.return_value = {'gridId': 1, 'rows': 10, 'columns': 20}
        mock_get.return_value = mock_response

        grid = service.get_grid_by_id(1)

        assert isinstance(grid, Grid)
        assert grid.get_grid_id() == 1
        assert grid.get_rows() == 10
        assert grid.get_columns() == 20
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/1")

    @patch('requests.get')
    def test_not_found(self, mock_get):
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = Exception()
        mock_get.return_value = mock_response

        with pytest.raises(Exception) as exc_info:
            grid = service.get_grid_by_id(1)
            assert grid is None

        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/1")

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
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/environment/1")

    @patch('requests.get')
    def test_empty_list(self, mock_get, mock_response):
        mock_response.json.return_value = []
        mock_get.return_value = mock_response

        grids = service.get_grids_in_environment(1)

        assert len(grids) == 0
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/environment/1")

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_grids_in_environment(1)
        assert str(exc_info.value) == "Test error"

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
        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/entity/1")

    @patch('requests.get')
    def test_not_found(self, mock_get):
        mock_response = Mock()
        mock_response.raise_for_status.side_effect = HTTPError(response=Mock(status_code=404))
        mock_get.return_value = mock_response

        with pytest.raises(Exception) as exc_info:
            grid = service.get_grid_of_entity(1)
            assert grid is None

        mock_get.assert_called_once_with(f"{BASE_URL}{API_PATH}/entity/1")

    @patch('requests.get')
    def test_error(self, mock_get):
        mock_get.side_effect = Exception("Test error")

        with pytest.raises(Exception) as exc_info:
            service.get_grid_of_entity(1)
        assert str(exc_info.value) == "Test error"