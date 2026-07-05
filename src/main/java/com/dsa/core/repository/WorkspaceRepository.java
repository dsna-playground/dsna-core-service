package com.dsa.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsa.core.model.UserWorkspace;

public interface WorkspaceRepository extends JpaRepository<UserWorkspace, Long> {

    List<UserWorkspace> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<UserWorkspace> findByIdAndUserId(Long id, Long userId);
}
