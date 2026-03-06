package com.eduardo.rpg.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduardo.rpg.entity.User;
import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {
    
    //busca o usuario pelo username
    Optional<User> findByUsername(String username);

    //busca o usuario pelo email
    Optional<User> findByEmail(String email);

    //verifica se o username ja existe
    boolean existsByUsername(String username);

    //verifica se o email ja existe
    boolean existsByEmail(String email);
    
}
