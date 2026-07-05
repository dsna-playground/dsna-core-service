package com.dsa.core.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dsa.core.dto.WorkspaceRequest;
import com.dsa.core.dto.WorkspaceResponse;
import com.dsa.core.model.User;
import com.dsa.core.model.UserWorkspace;
import com.dsa.core.repository.UserRepository;
import com.dsa.core.repository.WorkspaceRepository;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public WorkspaceController(
            WorkspaceRepository workspaceRepository,
            UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> saveWorkspace(
            @RequestBody WorkspaceRequest request,
            Authentication authentication) {
        validateRequest(request);
        User currentUser = getCurrentUser(authentication);
        boolean isNewWorkspace = request.getId() == null;

        UserWorkspace workspace = isNewWorkspace
                ? new UserWorkspace()
                : workspaceRepository.findByIdAndUserId(request.getId(), currentUser.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Workspace not found"));

        workspace.setUserId(currentUser.getId());
        workspace.setWorkspaceName(normalizeOptionalText(request.getWorkspaceName()));
        workspace.setDataStructureType(request.getDataStructureType().trim());
        workspace.setStructureState(request.getStructureState());

        UserWorkspace savedWorkspace = workspaceRepository.save(workspace);
        HttpStatus responseStatus = isNewWorkspace ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(responseStatus)
                .body(WorkspaceResponse.from(savedWorkspace));
    }

    @GetMapping
    public List<WorkspaceResponse> getWorkspaces(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return workspaceRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUser.getId())
                .stream()
                .map(WorkspaceResponse::from)
                .toList();
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "A valid bearer token is required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user no longer exists"));
    }

    private void validateRequest(WorkspaceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getDataStructureType() == null
                || request.getDataStructureType().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Data structure type is required");
        }
        if (request.getStructureState() == null || request.getStructureState().isNull()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Structure state is required");
        }
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
