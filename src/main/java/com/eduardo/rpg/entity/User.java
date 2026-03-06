package com.eduardo.rpg.entity;

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

@Getter //Cria os getters
@Setter //Cria os setters
@NoArgsConstructor //Cria o construtor vazio
@AllArgsConstructor //Cria o construtor com todos os parâmetros
@Entity //Cria a entidade que representa a tabela no banco
@Table(name = "users") //define o nome da tabela
@EqualsAndHashCode(onlyExplicitlyIncluded = true) //Cria o equals e o hashCode, faz identificar pelo id
public class User {
    @Id //define a chave primaria
    @EqualsAndHashCode.Include //Cria o equals e o hashCode
    @GeneratedValue(strategy = GenerationType.IDENTITY) //define a geração automática da chave primaria
    private Long id;

    @NotBlank //Garante que o campo não seja vazio ou só preenchido por espaços
    @Size(min = 3, max = 50) //Define o limite de caracteres
    @Column(name = "username", unique = true, nullable=false, length=50, updatable=false) //Configuração da coluna (nome, unico, nulo, tamanho)
    private String username;

    @NotBlank
    @Email //Verifica se segue o padrão do email
    @Column(name = "email", unique = true, nullable=false, length = 100)
    private String email;

    @NotBlank
    @Column(name = "password", nullable=false, length = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  //Evita que o campo seja exibido no GET
    private String password;

    @Enumerated(EnumType.STRING) //Configuração do enum (limita as opções) para string
    @Column(name = "role", nullable=false)
    private Role role;

    @Column(updatable=false, nullable=false)
    @CreationTimestamp //Preenche o campo com a data e hora atual automaticamente, na criação
    private LocalDateTime createdAt;

    @Column(nullable=false)
    @UpdateTimestamp //Preenche o campo com a data e hora atual automaticamente, toda vez que algo alterar o campo
    private LocalDateTime updatedAt;
}
