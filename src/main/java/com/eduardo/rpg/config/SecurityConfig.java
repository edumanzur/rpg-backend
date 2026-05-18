package com.eduardo.rpg.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;
    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;
    
    //Criptografia
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Segurança
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // Libera o console do H2
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll() // Libera o Banco de Dados
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/users", "/users/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                // GET endpoints for races, character-classes, ability-spells, equipments são acessíveis a qualquer um autenticado
                // POST/PUT/DELETE são protegidos por @PreAuthorize nos controllers
                .requestMatchers(HttpMethod.GET, "/races/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/character-classes/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/ability-spells/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/equipments/**").authenticated()
                // POST/PUT/DELETE devem vir através de autenticação + @PreAuthorize
                .requestMatchers(HttpMethod.POST, "/races", "/character-classes", "/ability-spells", "/equipments").authenticated()
                .requestMatchers(HttpMethod.PUT, "/races/**", "/character-classes/**", "/ability-spells/**", "/equipments/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/races/**", "/character-classes/**", "/ability-spells/**", "/equipments/**").authenticated()
                .requestMatchers("/campaigns/*/status-templates/**").authenticated()
                .requestMatchers("/characters/*/statuses/**").authenticated()
                .requestMatchers("/campaigns/**").authenticated()
                .requestMatchers("/characters/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/sessions").hasAnyRole("MASTER", "ADMIN")
                .requestMatchers("/sessions/**").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated() // O resto continua protegido
            )
            .httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer(
                   conf -> conf.jwt(Customizer.withDefaults())
            );
        
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        var jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
