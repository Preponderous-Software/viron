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
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments", headers={})

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
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments/1", headers={})

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
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments/name/Environment1", headers={})

@pytest.mark.parametrize("name, encoded", [
    ("alpha?beta", "alpha%3Fbeta"),
    ("alpha#1", "alpha%231"),
    ("a/b", "a%2Fb"),
    ("../1", "..%2F1"),
    ("50%", "50%25"),
    ("two words", "two%20words"),
])
@patch('requests.get')
def test_get_environment_by_name_keeps_the_name_one_path_segment(mock_get, name, encoded):
    mock_response = Mock()
    mock_response.json.return_value = {'environmentId': 1, 'name': name, 'creationDate': '2024-01-01'}
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    service.get_environment_by_name(name)

    mock_get.assert_called_once_with(f"http://localhost:9999/api/v1/environments/name/{encoded}", headers={})

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
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments/entity/1", headers={})

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
    mock_post.assert_called_once_with(
        "http://localhost:9999/api/v1/environments",
        json={"name": "TestEnv", "numGrids": 10, "gridSize": 10},
        headers={},
    )


@patch('requests.post')
def test_create_environment_with_independent_dimensions(mock_post):
    mock_response = Mock()
    mock_response.json.return_value = {
        'environmentId': 2,
        'name': 'WideEnv',
        'creationDate': '2024-01-01'
    }
    mock_response.raise_for_status = Mock()
    mock_post.return_value = mock_response

    environment = service.create_environment('WideEnv', 1, num_rows=3, num_columns=12)

    assert isinstance(environment, Environment)
    mock_post.assert_called_once_with(
        "http://localhost:9999/api/v1/environments",
        json={"name": "WideEnv", "numGrids": 1, "numRows": 3, "numColumns": 12},
        headers={},
    )


@patch('requests.post')
def test_create_environment_without_dimensions_raises(mock_post):
    with pytest.raises(ValueError):
        service.create_environment('BadEnv', 1)

    mock_post.assert_not_called()


@patch('requests.post')
def test_create_environment_with_half_specified_dimensions_raises(mock_post):
    with pytest.raises(ValueError):
        service.create_environment('BadEnv', 1, num_rows=3)

    with pytest.raises(ValueError):
        service.create_environment('BadEnv', 1, grid_size=5, num_columns=3)

    mock_post.assert_not_called()


@patch('requests.delete')
def test_delete_environment(mock_delete):
    mock_response = Mock()
    mock_response.status_code = 204
    mock_response.raise_for_status = Mock()
    mock_delete.return_value = mock_response

    result = service.delete_environment(1)

    assert result is True
    mock_delete.assert_called_once_with("http://localhost:9999/api/v1/environments/1", headers={})

@patch('requests.patch')
def test_update_environment_name(mock_patch):
    mock_response = Mock()
    mock_response.status_code = 200
    mock_response.raise_for_status = Mock()
    mock_patch.return_value = mock_response

    result = service.update_environment_name(1, 'NewName')

    assert result is True
    mock_patch.assert_called_once_with(
        "http://localhost:9999/api/v1/environments/1/name",
        json={"name": "NewName"},
        headers={},
    )

@patch('requests.get')
def test_get_all_environments_empty(mock_get):
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    environments = service.get_all_environments()

    assert len(environments) == 0
    mock_get.assert_called_once_with("http://localhost:9999/api/v1/environments", headers={})

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

def test_get_auth_headers_without_token():
    assert service.get_auth_headers() == {}

@patch('requests.get')
def test_get_all_environments_sends_bearer_token(mock_get):
    authed_service = EnvironmentService("http://localhost", 9999, auth_token="test-token")
    mock_response = Mock()
    mock_response.json.return_value = []
    mock_response.raise_for_status = Mock()
    mock_get.return_value = mock_response

    authed_service.get_all_environments()

    mock_get.assert_called_once_with(
        "http://localhost:9999/api/v1/environments",
        headers={"Authorization": "Bearer test-token"},
    )


# Failure paths. Like GridService and unlike EntityService.update_entity_name, every
# EnvironmentService method lets the error from requests or from raise_for_status propagate as
# it is, so a caller can catch HTTPError on the writes as well as on the reads. None of the
# methods guards an empty body.
def _error_response(status):
    response = Mock()
    response.raise_for_status.side_effect = requests.exceptions.HTTPError(f"{status} Error")
    return response


@pytest.mark.parametrize("call", [
    lambda: service.get_environment_by_id(99),
    lambda: service.get_environment_by_name("missing"),
    lambda: service.get_environment_of_entity(99),
], ids=["by_id", "by_name", "of_entity"])
@patch('requests.get')
def test_lookup_not_found_raises_http_error_without_reading_body(mock_get, call):
    mock_get.return_value = _error_response(404)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        call()
    assert exc_info.type is requests.exceptions.HTTPError
    assert "404" in str(exc_info.value)
    mock_get.return_value.json.assert_not_called()


@patch('requests.post')
def test_create_environment_rejected_raises_http_error_unwrapped(mock_post):
    mock_post.return_value = _error_response(400)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        service.create_environment('x' * 256, 1, 5)
    assert exc_info.type is requests.exceptions.HTTPError
    assert "400" in str(exc_info.value)
    mock_post.return_value.json.assert_not_called()


@patch('requests.post')
def test_create_environment_network_error_propagates_unwrapped(mock_post):
    mock_post.side_effect = requests.exceptions.ConnectionError("connection refused")

    with pytest.raises(requests.exceptions.ConnectionError) as exc_info:
        service.create_environment('TestEnv', 1, 5)
    assert str(exc_info.value) == "connection refused"


@patch('requests.delete')
def test_delete_environment_not_found_raises_http_error(mock_delete):
    mock_delete.return_value = _error_response(404)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        service.delete_environment(99)
    assert "404" in str(exc_info.value)


# The server answers a successful delete 204. The client never reads the status code, so any
# status that raise_for_status lets through returns True.
@patch('requests.delete')
def test_delete_environment_returns_true_without_checking_status(mock_delete):
    mock_response = Mock()
    mock_response.status_code = 200
    mock_delete.return_value = mock_response

    assert service.delete_environment(1) is True


@pytest.mark.parametrize("status, name", [(404, "NewName"), (400, "x" * 256)], ids=["404", "400"])
@patch('requests.patch')
def test_update_environment_name_error_raises_http_error_unwrapped(mock_patch, status, name):
    mock_patch.return_value = _error_response(status)

    with pytest.raises(requests.exceptions.HTTPError) as exc_info:
        service.update_environment_name(99, name)
    assert exc_info.type is requests.exceptions.HTTPError
    assert str(status) in str(exc_info.value)


@patch('requests.patch')
def test_update_environment_name_network_error_propagates_unwrapped(mock_patch):
    mock_patch.side_effect = requests.exceptions.ConnectionError("connection refused")

    with pytest.raises(requests.exceptions.ConnectionError) as exc_info:
        service.update_environment_name(1, 'NewName')
    assert str(exc_info.value) == "connection refused"


# EnvironmentController.updateEnvironmentName is a void handler, so the server answers 200
# today. Any other 2xx makes the client return False without raising.
@patch('requests.patch')
def test_update_environment_name_other_success_status_returns_false(mock_patch):
    mock_response = Mock()
    mock_response.status_code = 204
    mock_patch.return_value = mock_response

    assert service.update_environment_name(1, 'NewName') is False


AUTH_HEADERS = {"Authorization": "Bearer test-token"}


def _authed_service():
    return EnvironmentService("http://localhost", 9999, auth_token="test-token")


@patch('requests.post')
def test_create_environment_sends_bearer_token(mock_post):
    mock_post.return_value.json.return_value = {
        'environmentId': 1, 'name': 'TestEnv', 'creationDate': '2024-01-01'
    }

    _authed_service().create_environment('TestEnv', 1, 5)

    mock_post.assert_called_once_with(
        "http://localhost:9999/api/v1/environments",
        json={"name": "TestEnv", "numGrids": 1, "gridSize": 5},
        headers=AUTH_HEADERS,
    )


@patch('requests.delete')
def test_delete_environment_sends_bearer_token(mock_delete):
    _authed_service().delete_environment(1)

    mock_delete.assert_called_once_with("http://localhost:9999/api/v1/environments/1", headers=AUTH_HEADERS)


@patch('requests.patch')
def test_update_environment_name_sends_bearer_token(mock_patch):
    mock_patch.return_value.status_code = 200

    _authed_service().update_environment_name(1, 'NewName')

    mock_patch.assert_called_once_with(
        "http://localhost:9999/api/v1/environments/1/name",
        json={"name": "NewName"},
        headers=AUTH_HEADERS,
    )


# Current behaviour, which differs from EntityService: an empty (null) body is not mapped to
# None or [], so the model constructor or the list comprehension raises TypeError.
@pytest.mark.parametrize("call", [
    lambda: service.get_environment_by_id(1),
    lambda: service.get_environment_by_name("Environment1"),
    lambda: service.get_environment_of_entity(1),
    lambda: service.get_all_environments(),
], ids=["by_id", "by_name", "of_entity", "all"])
@patch('requests.get')
def test_empty_body_raises_type_error(mock_get, call):
    mock_get.return_value.json.return_value = None

    with pytest.raises(TypeError):
        call()