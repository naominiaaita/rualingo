package com.example.rualingo.service;

import com.example.rualingo.DTO.RoleDTO;
import com.example.rualingo.DTO.UserDTO;
import com.example.rualingo.model.Role;
import com.example.rualingo.repository.RoleRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleDTO createRole(RoleDTO dto) {
        validateNewRole(dto);
        Role savedRole = roleRepository.save(toEntity(dto));
        return toDTO(savedRole);
    }

    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long roleId) {
        return toDTO(requireRole(roleId));
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RoleDTO> getRoleByName(String name) {
        String requiredName = Objects.requireNonNull(name, "name must not be null");
        return roleRepository.findByName(requiredName).map(this::toDTO);
    }

    public RoleDTO updateRole(Long roleId, RoleDTO dto) {
        Role role = requireRole(roleId);

        if (dto.getName() != null && !dto.getName().isBlank() && !dto.getName().equals(role.getName())) {
            validateUniqueRoleName(dto.getName());
            role.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }

        Role savedRole = Objects.requireNonNull(roleRepository.save(role), "Saved role must not be null");
        return toDTO(savedRole);
    }

    public void deleteRole(Long roleId) {
        Role role = requireRole(roleId);
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersForRole(Long roleId) {
        return requireRole(roleId).getUsers().stream()
                .map(user -> new UserDTO(
                        user.getUsername(),
                        user.getEmail(),
                        null,
                        user.getFirstName(),
                        user.getSecondName(),
                        user.getGender(),
                        null,
                        null,
                        user.isActive(),
                        user.getProfilePicture()))
                .collect(Collectors.toList());
    }

    public RoleDTO toDTO(Role role) {
        return new RoleDTO(role.getId(), role.getName(), role.getDescription());
    }

    public Role toEntity(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        return role;
    }

    public void validateNewRole(RoleDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("RoleDTO must not be null.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Role name is required.");
        }
        validateUniqueRoleName(dto.getName());
    }

    public void validateUniqueRoleName(String name) {
        String requiredName = Objects.requireNonNull(name, "name must not be null");
        if (roleRepository.existsByName(requiredName)) {
            throw new IllegalArgumentException("Role name is already in use.");
        }
    }

    private Role requireRole(Long roleId) {
        Long requiredRoleId = Objects.requireNonNull(roleId, "roleId must not be null");
        return roleRepository.findById(requiredRoleId)
                .orElseThrow(() -> new NoSuchElementException("Role not found: " + roleId));
    }
}
