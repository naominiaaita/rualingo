package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rualingo.model.Login;
import com.example.rualingo.model.User;
import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Optional<Login> findByUser(User user);
}
