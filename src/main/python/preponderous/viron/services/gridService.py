# Copyright (c) 2024 Preponderous Software
# MIT License

import logging
from typing import List, Optional
import requests
from requests.exceptions import HTTPError
from src.main.python.preponderous.viron.models.grid import Grid

logger = logging.getLogger(__name__)

class GridService:
    def __init__(self, viron_host: str, viron_port: int, auth_token: Optional[str] = None):
        self.viron_host = viron_host
        self.viron_port = viron_port
        self.auth_token = auth_token

    def get_base_url(self) -> str:
        return f"{self.viron_host}:{self.viron_port}/api/v1/grids"

    def get_auth_headers(self) -> dict:
        return {"Authorization": f"Bearer {self.auth_token}"} if self.auth_token else {}

    def get_all_grids(self) -> List[Grid]:
        response = requests.get(self.get_base_url(), headers=self.get_auth_headers())
        response.raise_for_status()
        return [Grid(**grid) for grid in response.json()]

    def get_grid_by_id(self, grid_id: int) -> Optional[Grid]:
        response = requests.get(f"{self.get_base_url()}/{grid_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return Grid(**response.json())

    def get_grids_in_environment(self, environment_id: int) -> List[Grid]:
        response = requests.get(f"{self.get_base_url()}/environment/{environment_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return [Grid(**grid) for grid in response.json()]

    def get_grid_of_entity(self, entity_id: int) -> Optional[Grid]:
        response = requests.get(f"{self.get_base_url()}/entity/{entity_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return Grid(**response.json())