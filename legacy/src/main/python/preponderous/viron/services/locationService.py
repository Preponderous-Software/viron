from typing import List, Optional
import logging
import requests
from dataclasses import dataclass
from src.main.python.preponderous.viron.models.location import Location

class LocationService:
    def __init__(self, viron_host: str, viron_port: int):
        self.viron_host = viron_host
        self.viron_port = viron_port

    def get_base_url(self) -> str:
        return f"{self.viron_host}:{self.viron_port}/api/v1/locations"

    def get_all_locations(self) -> List[Location]:
        response = requests.get(self.get_base_url())
        response.raise_for_status()
        return [Location(**loc) for loc in response.json()]

    def get_location_by_id(self, id: int) -> Location:
        response = requests.get(f"{self.get_base_url()}/{id}")
        if response.status_code == 404:
            raise Exception(f"Location not found with id: {id}")
        response.raise_for_status()
        return Location(**response.json())

    def get_locations_in_environment(self, environment_id: int) -> List[Location]:
        response = requests.get(f"{self.get_base_url()}/environment/{environment_id}")
        response.raise_for_status()
        return [Location(**loc) for loc in response.json()]

    def get_locations_in_grid(self, grid_id: int) -> List[Location]:
        response = requests.get(f"{self.get_base_url()}/grid/{grid_id}")
        response.raise_for_status()
        return [Location(**loc) for loc in response.json()]

    def get_location_of_entity(self, entity_id: int) -> Location:
        response = requests.get(f"{self.get_base_url()}/entity/{entity_id}")
        if response.status_code == 404:
            raise Exception(f"Location not found for entity: {entity_id}")
        response.raise_for_status()
        return Location(**response.json())

    def add_entity_to_location(self, entity_id: int, location_id: int) -> None:
        response = requests.put(f"{self.get_base_url()}/{location_id}/entity/{entity_id}")
        if response.status_code == 404:
            raise Exception("Location or entity not found")
        response.raise_for_status()

    def remove_entity_from_location(self, entity_id: int, location_id: int) -> None:
        response = requests.delete(f"{self.get_base_url()}/{location_id}/entity/{entity_id}")
        if response.status_code == 404:
            raise Exception("Location or entity not found")
        response.raise_for_status()

    def remove_entity_from_current_location(self, entity_id: int) -> None:
        response = requests.delete(f"{self.get_base_url()}/entity/{entity_id}")
        if response.status_code == 404:
            raise Exception("Entity not found")
        response.raise_for_status()