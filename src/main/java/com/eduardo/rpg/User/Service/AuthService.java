package com.eduardo.rpg.User.Service;

import com.eduardo.rpg.User.Domains.UserAuth;
import com.eduardo.rpg.User.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserAuth::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
