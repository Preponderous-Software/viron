
# Copyright (c) 2024 Preponderous Software
# MIT License

import requests
from src.main.python.preponderous.viron.models.environment import Environment

class EnvironmentService:
    def __init__(self, viron_host: str, viron_port: int):
        self.viron_host = viron_host
        self.viron_port = viron_port

    def get_base_url(self) -> str:
        return f"{self.viron_host}:{self.viron_port}/api/v1/environments"

    def get_all_environments(self) -> list[Environment]:
        response = requests.get(f"{self.get_base_url()}")
        response.raise_for_status()
        return [Environment(**env) for env in response.json()]

    def get_environment_by_id(self, environment_id: int) -> Environment:
        response = requests.get(f"{self.get_base_url()}/{environment_id}")
        response.raise_for_status()
        return Environment(**response.json())

    def get_environment_by_name(self, name: str) -> Environment:
        response = requests.get(f"{self.get_base_url()}/name/{name}")
        response.raise_for_status()
        return Environment(**response.json())

    def get_environment_of_entity(self, entity_id: int) -> Environment:
        response = requests.get(f"{self.get_base_url()}/entity/{entity_id}")
        response.raise_for_status()
        return Environment(**response.json())

    def create_environment(self, name: str, num_grids: int, grid_size: int) -> Environment:
        response = requests.post(f"{self.get_base_url()}/{name}/{num_grids}/{grid_size}")
        response.raise_for_status()
        return Environment(**response.json())

    def delete_environment(self, environment_id: int) -> bool:
        response = requests.delete(f"{self.get_base_url()}/{environment_id}")
        response.raise_for_status()
        return response.status_code == 200

    def update_environment_name(self, environment_id: int, name: str) -> bool:
        response = requests.patch(f"{self.get_base_url()}/{environment_id}/name/{name}")
        response.raise_for_status()
        return response.status_code == 200