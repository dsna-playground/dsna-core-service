package com.dsa.core.dto;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class WorkspaceRequest {

    private Long id;
    private String workspaceName;
    private String dataStructureType;
    private JsonNode structureState;
}
