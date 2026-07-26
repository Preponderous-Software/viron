
# Copyright (c) 2024 Preponderous Software
# MIT License

from typing import Optional
import requests
from src.main.python.preponderous.viron.models.environment import Environment

class EnvironmentService:
    def __init__(self, viron_host: str, viron_port: int, auth_token: Optional[str] = None):
        self.viron_host = viron_host
        self.viron_port = viron_port
        self.auth_token = auth_token

    def get_base_url(self) -> str:
        return f"{self.viron_host}:{self.viron_port}/api/v1/environments"

    def get_auth_headers(self) -> dict:
        return {"Authorization": f"Bearer {self.auth_token}"} if self.auth_token else {}

    def get_all_environments(self) -> list[Environment]:
        response = requests.get(f"{self.get_base_url()}", headers=self.get_auth_headers())
        response.raise_for_status()
        return [Environment(**env) for env in response.json()]

    def get_environment_by_id(self, environment_id: int) -> Environment:
        response = requests.get(f"{self.get_base_url()}/{environment_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return Environment(**response.json())

    def get_environment_by_name(self, name: str) -> Environment:
        response = requests.get(f"{self.get_base_url()}/name/{name}", headers=self.get_auth_headers())
        response.raise_for_status()
        return Environment(**response.json())

    def get_environment_of_entity(self, entity_id: int) -> Environment:
        response = requests.get(f"{self.get_base_url()}/entity/{entity_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return Environment(**response.json())

    def create_environment(self, name: str, num_grids: int, grid_size: Optional[int] = None,
                           num_rows: Optional[int] = None, num_columns: Optional[int] = None) -> Environment:
        """Create an environment containing num_grids grids.

        Supply either grid_size (square shorthand) or both num_rows and num_columns.
        num_rows/num_columns take precedence over grid_size; supplying only one of them
        is rejected by the API with a 400.
        """
        if (num_rows is None) != (num_columns is None):
            raise ValueError("num_rows and num_columns must be provided together")
        if num_rows is None and grid_size is None:
            raise ValueError("either grid_size or both num_rows and num_columns must be provided")

        payload = {"name": name, "numGrids": num_grids}
        if grid_size is not None:
            payload["gridSize"] = grid_size
        if num_rows is not None:
            payload["numRows"] = num_rows
        if num_columns is not None:
            payload["numColumns"] = num_columns

        response = requests.post(f"{self.get_base_url()}", json=payload, headers=self.get_auth_headers())
        response.raise_for_status()
        return Environment(**response.json())

    def delete_environment(self, environment_id: int) -> bool:
        response = requests.delete(f"{self.get_base_url()}/{environment_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return True

    def update_environment_name(self, environment_id: int, name: str) -> bool:
        response = requests.patch(f"{self.get_base_url()}/{environment_id}/name", json={"name": name}, headers=self.get_auth_headers())
        response.raise_for_status()
        return response.status_code == 200