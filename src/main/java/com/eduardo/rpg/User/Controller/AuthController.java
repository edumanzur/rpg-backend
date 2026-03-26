package com.eduardo.rpg.User.Controller;


import com.eduardo.rpg.User.DTO.AuthRequest;
import com.eduardo.rpg.User.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/authentication")
    public ResponseEntity<String> authenticate(@RequestBody @Valid AuthRequest request) {
        String token = authService.authenticate(request);
        return ResponseEntity.ok(token);
    }
}
