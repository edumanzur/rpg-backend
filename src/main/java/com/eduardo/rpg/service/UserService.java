package com.eduardo.rpg.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduardo.rpg.repository.UserRepository;
import com.eduardo.rpg.entity.User;
import com.eduardo.rpg.enums.Role;

import com.eduardo.rpg.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import com.eduardo.rpg.dto.user.CreateUserRequest;

import java.util.List;

import com.eduardo.rpg.dto.user.UserResponseDTO;

import com.eduardo.rpg.exception.UserAlreadyExistsException;

import com.eduardo.rpg.dto.user.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    //Cria o usuario
    @Transactional //Para garantir a integridade dos dados
    public UserResponseDTO createUser(CreateUserRequest dto) {

        //Verifica se o username já existe
        if (userRepository.existsByUsername(dto.username())) {
            throw new UserAlreadyExistsException("Username " + dto.username() + " already exists");
        }

        //Verifica se o email já existe
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("Email " + dto.email() + " already exists");
        }

        //Converte o Request para a Entidade
        User user = userMapper.toEntity(dto);
    
        //Criptografa a senha
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(Role.PLAYER);
        
        //Salva
        User savedUser = userRepository.save(user);
        
        //Converte a Entidade para o DTO
        return userMapper.toResponse(savedUser);
    }

    //Encontra o usuario pelo id
    @Transactional(readOnly = true) //Para garantir a integridade dos dados (para buscas)
    public UserResponseDTO findUserById(Long id) {
        if(id == null){
            throw new ResourceNotFoundException("Usuário nao encontrado!");
        }
        return userRepository.findById(id)
                .map(userMapper::toResponse) 
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
    }

    //Encontra todos os usuarios
    @Transactional(readOnly = true) //Para garantir a integridade dos dados (para buscas)
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(userMapper::toResponse)
            .toList();
    }

    //Deleta o usuario
    @Transactional
    public void deleteUser(Long id) {
        if(id == null){
            throw new ResourceNotFoundException("Usuário nao encontrado!");
        }
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
        userRepository.delete(user);
    }

}
