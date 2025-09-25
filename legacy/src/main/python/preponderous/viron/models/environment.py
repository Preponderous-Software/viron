# Copyright (c) 2024 Preponderous Software
# MIT License

import re


class Environment:
    def __init__(self, environmentId: int, name: str, creationDate: str):
        self.setEnvironmentId(environmentId)
        self.setName(name)
        self.setCreationDate(creationDate)
    
    def getEnvironmentId(self) -> int:
        return self.environmentId
    
    def setEnvironmentId(self, environmentId: int):
        if not isinstance(environmentId, int):
            raise TypeError("environmentId must be an integer")
        self.environmentId = environmentId
    
    def getName(self) -> str:
        return self.name
    
    def setName(self, name: str):
        if not isinstance(name, str):
            raise TypeError("name must be a string")
        self.name = name
        
    def getCreationDate(self) -> str:
        return self.creationDate
    
    def setCreationDate(self, creationDate: str):
        # if not re.match(r"\d{4}-\d{2}-\d{2}", creationDate):
        #     raise ValueError("creationDate must be in the format YYYY-MM-DD")
        self.creationDate = creationDate
        
    def __str__(self):
        return f"Environment{{environmentId={self.environmentId}, name={self.name}, creationDate={self.creationDate}}}"