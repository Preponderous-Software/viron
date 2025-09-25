import pytest
import requests
from unittest.mock import patch, Mock
from src.main.python.preponderous.viron.services.environmentService import EnvironmentService
from src.main.python.preponderous.viron.models.environment import Environment

service = EnvironmentService("http://localhost", 9999)


def test_init():
    assert service.viron_host == "http://localhost"
    assert service.viron_port == 9999


def test_get_base_url():
    expected = "http://localhost:9999/api/v1/environments"
    assert service.get_base_url() == expected

@patch('requests.get')
def test_get_all_environments(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = [
        {'environmentId': 1, 'name': 'Environment1', 'creationDate': '2024-01-01'},
        {'environmentId': 2, 'name': 'Environment2', 'creationDate': '2024-01-01'}
    ]
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    environments = service.get_all_environments()

    assert len(environments) == 2
    assert isinstance(environments[0], Environment)
    assert environments[0].getEnvironmentId() == 1
    assert environments[0].getName() == 'Environment1'
    assert environments[0].getCreationDate() == '2024-01-01'
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments")

@patch('requests.get')
def test_get_environment_by_id(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = {
        'environmentId': 1,
        'name': 'Environment1',
        'creationDate': '2024-01-01'
    }
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    environment = service.get_environment_by_id(1)

    assert isinstance(environment, Environment)
    assert environment.getEnvironmentId() == 1
    assert environment.getName() == 'Environment1'
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments/1")

@patch('requests.get')
def test_get_environment_by_name(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = {
        'environmentId': 1,
        'name': 'Environment1',
        'creationDate': '2024-01-01'
    }
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    environment = service.get_environment_by_name("Environment1")

    assert isinstance(environment, Environment)
    assert environment.getName() == 'Environment1'
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments/name/Environment1")

@patch('requests.get')
def test_get_environment_of_entity(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = {
        'environmentId': 1,
        'name': 'Environment1',
        'creationDate': '2024-01-01'
    }
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    environment = service.get_environment_of_entity(1)

    assert isinstance(environment, Environment)
    assert environment.getEnvironmentId() == 1
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments/entity/1")

@patch('requests.post')
def test_create_environment(mock_post):
    mock_response = Mock()
    mock_response.json.return_value = {
        'environmentId': 1,
        'name': 'TestEnv',
        'creationDate': '2024-01-01'
    }
    mock_response.raise_for_status = Mock()
    mock_post.return_value = mock_response

    environment = service.create_environment('TestEnv', 10, 10)

    assert isinstance(environment, Environment)
    assert environment.getName() == 'TestEnv'
    mock_post.assert_called_once_with("http://localhost:9999/api/v1/environments/TestEnv/10/10")

@patch('requests.delete')
def test_delete_environment(mock_delete):
    mock_response = Mock()
    mock_response.status_code = 200
    mock_response.raise_for_status = Mock()
    mock_delete.return_value = mock_response

    result = service.delete_environment(1)

    assert result is True
    mock_delete.assert_called_once_with("http://localhost:9999/api/v1/environments/1")

@patch('requests.patch')
def test_update_environment_name(mock_patch):
    mock_response = Mock()
    mock_response.status_code = 200
    mock_response.raise_for_status = Mock()
    mock_patch.return_value = mock_response

    result = service.update_environment_name(1, 'NewName')

    assert result is True
    mock_patch.assert_called_once_with("http://localhost:9999/api/v1/environments/1/name/NewName")

@patch('requests.get')
def test_get_all_environments_empty(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    environments = service.get_all_environments()

    assert len(environments) == 0
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments")

@patch('requests.get')
def test_http_error_handling(mock_get):
    mock_response = Mock()
    mock_response.raise_for_status.side_effect = requests.exceptions.HTTPError()
    mock_get.return_value = mock_response

    with pytest.raises(requests.exceptions.HTTPError):
        service.get_all_environments()

def test_environment_validation():
    # with pytest.raises(ValueError):
    #     Environment(1, "Test", "invalid-date")

    with pytest.raises(TypeError):
        Environment("1", "Test", "2024-01-01")

    with pytest.raises(TypeError):
        Environment(1, 123, "2024-01-01")