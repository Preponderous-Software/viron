
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

    def create_environment(self, name: str, num_grids: int, grid_size: int) -> Environment:
        payload = {"name": name, "numGrids": num_grids, "gridSize": grid_size}
        response = requests.post(f"{self.get_base_url()}", json=payload, headers=self.get_auth_headers())
        response.raise_for_status()
        return Environment(**response.json())

    def delete_environment(self, environment_id: int) -> bool:
        response = requests.delete(f"{self.get_base_url()}/{environment_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return response.status_code == 200

    def update_environment_name(self, environment_id: int, name: str) -> bool:
        response = requests.patch(f"{self.get_base_url()}/{environment_id}/name", json={"name": name}, headers=self.get_auth_headers())
        response.raise_for_status()
        return response.status_code == 200