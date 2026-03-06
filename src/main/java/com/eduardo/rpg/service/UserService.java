package com.eduardo.rpg.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduardo.rpg.repository.UserRepository;
import com.eduardo.rpg.entity.User;
import com.eduardo.rpg.enums.Role;

import com.eduardo.rpg.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //Cria o usuario
    @Transactional //Para garantir a integridade dos dados
    public User createUser(User user) {

        //Verifica se o username já existe
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        //Verifica se o email já existe
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        //Criptografa a senha
        String encodedPassword = passwordEncoder.encode(user.getPassword()); //Troca a senha pelo hash 
        user.setPassword(encodedPassword); //Salva a senha criptografada

        //Define o role
        user.setRole(Role.PLAYER);

        return userRepository.save(user);
    }

    //Encontra o usuario pelo id
    @Transactional(readOnly = true) //Para garantir a integridade dos dados (para buscas)
    public User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
    }

    //Encontra todos os usuarios
    @Transactional(readOnly = true) //Para garantir a integridade dos dados (para buscas)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    //Deleta o usuario
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }


}
