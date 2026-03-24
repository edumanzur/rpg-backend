    package com.eduardo.rpg.User.Domains;
    
    import com.eduardo.rpg.enums.Role;
    
    import java.time.LocalDateTime;
    
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;
    
    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.Enumerated;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import lombok.AllArgsConstructor;
    import lombok.EqualsAndHashCode;
    import lombok.Getter;
    import lombok.Setter;
    import lombok.NoArgsConstructor;
    import jakarta.persistence.EnumType;
    import jakarta.persistence.Table;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Size;
    import com.fasterxml.jackson.annotation.JsonProperty;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "tb_users")
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public class User {
        @Id
        @EqualsAndHashCode.Include
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        @NotBlank
        @Size(min = 3, max = 50)
        @Column(name = "username", unique = true, nullable=false, length=50, updatable=false)
        private String username;
    
        @NotBlank
        @Email
        @Column(name = "email", unique = true, nullable=false, length = 100)
        private String email;
    
        @NotBlank
        @Column(name = "password", nullable=false)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String password;
    
        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable=false, length = 20)
        private Role role;
    
        @Column(updatable=false, nullable=false)
        @CreationTimestamp
        private LocalDateTime createdAt;
    
        @Column(nullable=false)
        @UpdateTimestamp
        private LocalDateTime updatedAt;

    }
