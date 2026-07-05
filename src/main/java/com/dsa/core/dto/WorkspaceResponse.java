package com.dsa.core.dto;

import java.time.LocalDateTime;

import com.dsa.core.model.UserWorkspace;
import com.fasterxml.jackson.databind.JsonNode;

public record WorkspaceResponse(
        Long id,
        String workspaceName,
        String dataStructureType,
        JsonNode structureState,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static WorkspaceResponse from(UserWorkspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getWorkspaceName(),
                workspace.getDataStructureType(),
                workspace.getStructureState(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt());
    }
}
