package com.eduardo.rpg.CharacterStatus;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tb_character_statuses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"character_id", "template_id"})
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CharacterStatus {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private StatusTemplate template;

    @Column(name = "current_value", nullable = false)
    private Integer currentValue = 0;

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

