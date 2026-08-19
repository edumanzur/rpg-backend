package com.eduardo.rpg.User.Controller;

import com.eduardo.rpg.User.DTO.CreateUserRequest;
import com.eduardo.rpg.User.DTO.UserResponseDTO;
import com.eduardo.rpg.User.Service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Encontra o usuario pelo id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        UserResponseDTO response = userService.findUserById(id);
        return ResponseEntity.ok(response);
    }
    
    //Encontra todos os usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> findAllUsers(Pageable pageable) {
        Page<UserResponseDTO> response = userService.findAllUsers(pageable);
        return ResponseEntity.ok(response);
    }
    
    //Cria o usuario
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserRequest dto) {
        UserResponseDTO response = userService.createUser(dto);

        //Retorna o status 201 (Criado)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    //Deleta o usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        //Retorna o status 204 (No Content)
        return ResponseEntity.noContent().build();
    }

    //Atualiza o usuario (próprio usuario ou admin)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(Authentication authentication, @PathVariable Long id, @RequestBody @Valid com.eduardo.rpg.User.DTO.UpdateUserRequest dto) {
        UserResponseDTO response = userService.updateUser(authentication, id, dto);
        return ResponseEntity.ok(response);
    }
}
