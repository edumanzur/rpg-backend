package com.eduardo.rpg.User.Controller;


import com.eduardo.rpg.User.Service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) { this.userService = userService; }
    
}
