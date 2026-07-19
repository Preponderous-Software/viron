from typing import List, Optional
import logging
import requests
from dataclasses import dataclass
from src.main.python.preponderous.viron.models.location import Location

class LocationService:
    def __init__(self, viron_host: str, viron_port: int, auth_token: Optional[str] = None):
        self.viron_host = viron_host
        self.viron_port = viron_port
        self.auth_token = auth_token

    def get_base_url(self) -> str:
        return f"{self.viron_host}:{self.viron_port}/api/v1/locations"

    def get_auth_headers(self) -> dict:
        return {"Authorization": f"Bearer {self.auth_token}"} if self.auth_token else {}

    def get_all_locations(self) -> List[Location]:
        response = requests.get(self.get_base_url(), headers=self.get_auth_headers())
        response.raise_for_status()
        return [Location(**loc) for loc in response.json()]

    def get_location_by_id(self, id: int) -> Location:
        response = requests.get(f"{self.get_base_url()}/{id}", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception(f"Location not found with id: {id}")
        response.raise_for_status()
        return Location(**response.json())

    def get_locations_in_environment(self, environment_id: int) -> List[Location]:
        response = requests.get(f"{self.get_base_url()}/environment/{environment_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return [Location(**loc) for loc in response.json()]

    def get_locations_in_grid(self, grid_id: int) -> List[Location]:
        response = requests.get(f"{self.get_base_url()}/grid/{grid_id}", headers=self.get_auth_headers())
        response.raise_for_status()
        return [Location(**loc) for loc in response.json()]

    def get_location_of_entity(self, entity_id: int) -> Location:
        response = requests.get(f"{self.get_base_url()}/entity/{entity_id}", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception(f"Location not found for entity: {entity_id}")
        response.raise_for_status()
        return Location(**response.json())

    def add_entity_to_location(self, entity_id: int, location_id: int) -> None:
        response = requests.put(f"{self.get_base_url()}/{location_id}/entity/{entity_id}", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception("Location or entity not found")
        response.raise_for_status()

    def remove_entity_from_location(self, entity_id: int, location_id: int) -> None:
        response = requests.delete(f"{self.get_base_url()}/{location_id}/entity/{entity_id}", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception("Location or entity not found")
        response.raise_for_status()

    def remove_entity_from_current_location(self, entity_id: int) -> None:
        response = requests.delete(f"{self.get_base_url()}/entity/{entity_id}", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception("Entity not found")
        response.raise_for_status()

    def get_entity_ids_at_location(self, location_id: int) -> List[int]:
        response = requests.get(f"{self.get_base_url()}/{location_id}/entities", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception(f"Location not found with id: {location_id}")
        response.raise_for_status()
        return response.json()

    def is_location_occupied(self, location_id: int) -> bool:
        response = requests.get(f"{self.get_base_url()}/{location_id}/occupied", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception(f"Location not found with id: {location_id}")
        response.raise_for_status()
        return response.json()

    def move_entity_to_location(self, entity_id: int, location_id: int) -> None:
        response = requests.put(f"{self.get_base_url()}/{location_id}/entity/{entity_id}/move", headers=self.get_auth_headers())
        if response.status_code == 404:
            raise Exception("Entity or location not found")
        if response.status_code == 409:
            raise Exception(f"Target location {location_id} is already occupied")
        response.raise_for_status()