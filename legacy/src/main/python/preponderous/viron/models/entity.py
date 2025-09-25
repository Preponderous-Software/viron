# Copyright (c) 2024 Preponderous Software
# MIT License

import re


class Entity:
    def __init__(self, entityId: int, name: str, creationDate: str):
        self.setEntityId(entityId)
        self.setName(name)
        self.setCreationDate(creationDate)
        
    def getEntityId(self) -> int:
        return self.entityId
    
    def setEntityId(self, entityId: int):
        if not isinstance(entityId, int):
            raise TypeError("entityId must be an integer")
        self.entityId = entityId
    
    def getName(self) -> str:
        return self.name
    
    def setName(self, name: str):
        if not isinstance(name, str):
            raise TypeError("name must be a string")
        self.name = name
    
    def getCreationDate(self) -> str:
        return self.creationDate
    
    def setCreationDate(self, creationDate: str):
        if not re.match(r"\d{4}-\d{2}-\d{2}", creationDate):
            raise ValueError("creationDate must be in the format YYYY-MM-DD")
        self.creationDate = creationDate
    
    def __str__(self):
        return f"Entity{{entityId={self.entityId}, name={self.name}, creationDate={self.creationDate}}}"
