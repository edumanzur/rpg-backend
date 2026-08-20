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

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Each property value may be either a Spring resource location
    // (classpath:public.key, file:/path/to/key) used by local/dev, or the
    // raw PEM text itself, used in prod where the key is injected via an
    // env var (JWT_PUBLIC_KEY / JWT_PRIVATE_KEY) rather than baked into the
    // image as a file. See resolveKeyMaterial() below for the detection.
    @Value("${jwt.public.key}")
    private String publicKeyProperty;
    @Value("${jwt.private.key}")
    private String privateKeyProperty;

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private RSAPublicKey publicKey() {
        try {
            byte[] der = decodePem(resolveKeyMaterial(publicKeyProperty));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(der));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to load JWT public key", e);
        }
    }

    private RSAPrivateKey privateKey() {
        try {
            byte[] der = decodePem(resolveKeyMaterial(privateKeyProperty));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to load JWT private key", e);
        }
    }

    // If the configured value looks like a resource location (classpath:,
    // file:, or a URL), load it as a file (local/dev path, unchanged
    // behavior). Otherwise treat the value itself as literal PEM text
    // (prod path: JWT_PUBLIC_KEY / JWT_PRIVATE_KEY env vars holding the
    // PEM content directly).
    private String resolveKeyMaterial(String property) throws IOException {
        String trimmed = property.trim();
        if (trimmed.startsWith("-----BEGIN")) {
            return trimmed;
        }
        Resource resource = resourceLoader.getResource(trimmed);
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private byte[] decodePem(String pem) {
        String cleaned = pem
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }


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

    // Permite chamadas do frontend na(s) origin(s) configurada(s). Em local,
    // continua defaultando para http://localhost:8080 exatamente como antes;
    // em prod é sobrescrito via APP_CORS_ALLOWED_ORIGINS (lista separada por
    // vírgula) apontando para o domínio real do frontend.
    @Value("${app.cors.allowed-origins:http://localhost:8080}")
    private List<String> allowedOrigins;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        var jwk = new RSAKey.Builder(publicKey()).privateKey(privateKey()).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
