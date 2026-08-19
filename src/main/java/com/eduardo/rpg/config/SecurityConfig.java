package com.eduardo.rpg.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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
            .cors(Customizer.withDefaults())
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
                .requestMatchers(HttpMethod.POST, "/campaigns").authenticated() // Permite qualquer autenticado criar campanha
                .requestMatchers("/campaigns/**").authenticated()
                .requestMatchers("/characters/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/sessions").hasRole("ADMIN") // Apenas ADMIN (MASTER role não existe, use ADMIN)
                .requestMatchers("/sessions/**").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated() // O resto continua protegido
            )
            .httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer(
                   conf -> conf.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    // The access token's "scope" claim already stores full authorities like
    // "ROLE_ADMIN" (see JwtService#generateToken, mirroring UserAuth's own
    // GrantedAuthority). Spring's default JwtAuthenticationConverter prefixes
    // every scope value with "SCOPE_", which would turn that into
    // "SCOPE_ROLE_ADMIN" — never matching hasRole("ADMIN")/@PreAuthorize
    // checks. Use an empty prefix so the claim's own "ROLE_x" value is used
    // as-is.
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authoritiesConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // Permite chamadas do frontend em http://localhost:8080 (perfil local / desenvolvimento)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
