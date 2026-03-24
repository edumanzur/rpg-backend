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

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Encontra o usuario pelo id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        UserResponseDTO response = userService.findUserById(id);
        return ResponseEntity.ok(response);
    }
    
    //Encontra todos os usuarios
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAllUsers() {
        List<UserResponseDTO> response = userService.findAllUsers();
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
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        //Retorna o status 204 (No Content)
        return ResponseEntity.noContent().build();
    }
}
