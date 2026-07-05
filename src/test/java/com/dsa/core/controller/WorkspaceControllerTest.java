package com.dsa.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.dsa.core.dto.WorkspaceRequest;
import com.dsa.core.dto.WorkspaceResponse;
import com.dsa.core.model.User;
import com.dsa.core.model.UserWorkspace;
import com.dsa.core.repository.UserRepository;
import com.dsa.core.repository.WorkspaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@ExtendWith(MockitoExtension.class)
class WorkspaceControllerTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    private WorkspaceController workspaceController;
    private Authentication authentication;
    private User user;

    @BeforeEach
    void setUp() {
        workspaceController = new WorkspaceController(workspaceRepository, userRepository);
        authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        user = new User("alice", "unused-password", "alice@example.com");
        user.setId(42L);
    }

    @Test
    void savesNewWorkspaceForAuthenticatedUser() {
        ArrayNode state = new ObjectMapper().createArrayNode().add(5).add(2).add(8);
        WorkspaceRequest request = new WorkspaceRequest();
        request.setWorkspaceName("Sorting example");
        request.setDataStructureType("ARRAY");
        request.setStructureState(state);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(workspaceRepository.save(any(UserWorkspace.class))).thenAnswer(invocation -> {
            UserWorkspace workspace = invocation.getArgument(0);
            workspace.setId(10L);
            workspace.setCreatedAt(LocalDateTime.now());
            workspace.setUpdatedAt(LocalDateTime.now());
            return workspace;
        });

        var response = workspaceController.saveWorkspace(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(10L);

        ArgumentCaptor<UserWorkspace> workspaceCaptor =
                ArgumentCaptor.forClass(UserWorkspace.class);
        verify(workspaceRepository).save(workspaceCaptor.capture());
        assertThat(workspaceCaptor.getValue().getUserId()).isEqualTo(42L);
        assertThat(workspaceCaptor.getValue().getStructureState()).isEqualTo(state);
    }

    @Test
    void returnsOnlyAuthenticatedUsersWorkspaces() {
        UserWorkspace workspace = new UserWorkspace();
        workspace.setId(10L);
        workspace.setUserId(42L);
        workspace.setWorkspaceName("Sorting example");
        workspace.setDataStructureType("ARRAY");
        workspace.setStructureState(new ObjectMapper().createArrayNode().add(1).add(2));
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(workspaceRepository.findAllByUserIdOrderByUpdatedAtDesc(42L))
                .thenReturn(List.of(workspace));

        List<WorkspaceResponse> response =
                workspaceController.getWorkspaces(authentication);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(10L);
        verify(workspaceRepository).findAllByUserIdOrderByUpdatedAtDesc(42L);
    }
}
