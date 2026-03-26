package com.eduardo.rpg.User.Service;

import com.eduardo.rpg.User.Domains.UserAuth;
import com.eduardo.rpg.User.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(login, login)
                .map(UserAuth::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + login));
    }
}
