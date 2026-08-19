package com.eduardo.rpg.config;

import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
@Profile("local") // só roda no profile local
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Cria um usuário admin para testes locais, caso não exista
        String adminUsername = "admin";
        String adminEmail = "admin@local";
        String adminPassword = "admin123"; // senha para testes

        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("[DataInitializer] Usuário admin criado: username='" + adminUsername + "' password='" + adminPassword + "'");
        } else {
            System.out.println("[DataInitializer] Usuário admin já existe. Nada a fazer.");
        }
    }

}


