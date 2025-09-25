import logging
from typing import List, Optional
import requests

from src.main.python.preponderous.viron.models.entity import Entity

logger = logging.getLogger(__name__)

class EntityService:
    def __init__(self, viron_host: str, viron_port: int):
        self.viron_host = viron_host
        self.viron_port = viron_port

    def get_base_url(self) -> str:
        return f"{self.viron_host}:{self.viron_port}/api/v1/entities"

    def get_all_entities(self) -> List[Entity]:
        response = requests.get(self.get_base_url())
        response.raise_for_status()
        data = response.json()
        return [Entity(**entity) for entity in data] if data else []

    def get_entity_by_id(self, entity_id: int) -> Optional[Entity]:
        response = requests.get(f"{self.get_base_url()}/{entity_id}")
        response.raise_for_status()
        data = response.json()
        return Entity(**data) if data else None

    def get_entities_in_environment(self, environment_id: int) -> List[Entity]:
        response = requests.get(f"{self.get_base_url()}/environment/{environment_id}")
        response.raise_for_status()
        data = response.json()
        return [Entity(**entity) for entity in data] if data else []

    def get_entities_in_grid(self, grid_id: int) -> List[Entity]:
        response = requests.get(f"{self.get_base_url()}/grid/{grid_id}")
        response.raise_for_status()
        data = response.json()
        return [Entity(**entity) for entity in data] if data else []

    def get_entities_in_location(self, location_id: int) -> List[Entity]:
        response = requests.get(f"{self.get_base_url()}/location/{location_id}")
        response.raise_for_status()
        data = response.json()
        return [Entity(**entity) for entity in data] if data else []

    def get_entities_not_in_any_location(self) -> List[Entity]:
        response = requests.get(f"{self.get_base_url()}/unassigned")
        response.raise_for_status()
        data = response.json()
        return [Entity(**entity) for entity in data] if data else []

    def create_entity(self, name: str) -> Entity:
        response = requests.post(f"{self.get_base_url()}/{name}")
        response.raise_for_status()
        data = response.json()
        if not data:
            raise Exception("Created entity response was null")
        return Entity(**data)

    def delete_entity(self, entity_id: int) -> bool:
        try:
            response = requests.delete(f"{self.get_base_url()}/{entity_id}")
            response.raise_for_status()
            return True
        except Exception as e:
            logger.error(f"Failed to delete entity {entity_id}: {str(e)}")
            raise Exception("Error deleting entity", e)

    def update_entity_name(self, entity_id: int, name: str) -> bool:
        try:
            response = requests.patch(f"{self.get_base_url()}/{entity_id}/name/{name}")
            response.raise_for_status()
            return True
        except Exception as e:
            logger.error(f"Failed to update name for entity {entity_id}: {str(e)}")
            raise Exception("Error updating entity name", e)
