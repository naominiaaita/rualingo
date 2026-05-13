package com.example.rualingo.repository;

import com.example.rualingo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByAuthProviderAndProviderUserId(String authProvider, String providerUserId);
    List<User> findByRoleNameIgnoreCase(String roleName);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByRole_NameIgnoreCase(String roleName);
    long countByRole_NameIgnoreCase(String roleName);
}
