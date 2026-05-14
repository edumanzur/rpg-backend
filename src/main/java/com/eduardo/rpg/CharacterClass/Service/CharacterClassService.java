package com.eduardo.rpg.CharacterClass.Service;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.DTO.CharacterClassMapper;
import com.eduardo.rpg.CharacterClass.DTO.CharacterClassResponseDTO;
import com.eduardo.rpg.CharacterClass.DTO.CreateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.DTO.UpdateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CharacterClassService {

    private final CharacterClassRepository characterClassRepository;
    private final CharacterClassMapper characterClassMapper;

    @Transactional
    public CharacterClassResponseDTO createCharacterClass(CreateCharacterClassRequest dto) {
        if (characterClassRepository.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Já existe uma classe com este nome");
        }

        CharacterClass characterClass = characterClassMapper.toEntity(dto);
        CharacterClass savedCharacterClass = characterClassRepository.save(characterClass);
        return characterClassMapper.toResponse(savedCharacterClass);
    }

    @Transactional(readOnly = true)
    public CharacterClassResponseDTO findCharacterClassById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Classe não encontrada!");
        }

        CharacterClass characterClass = characterClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));
        return characterClassMapper.toResponse(characterClass);
    }

    @Transactional(readOnly = true)
    public Page<CharacterClassResponseDTO> findAllCharacterClasses(Pageable pageable) {
        return characterClassRepository.findAll(pageable)
            .map(characterClassMapper::toResponse);
    }

    @Transactional
    public CharacterClassResponseDTO updateCharacterClass(Long id, UpdateCharacterClassRequest dto) {
        CharacterClass characterClass = characterClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));

        characterClassRepository.findByNameIgnoreCase(dto.name())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe uma classe com este nome");
            });

        characterClass = characterClassMapper.toEntity(dto, characterClass);
        CharacterClass updatedCharacterClass = characterClassRepository.save(characterClass);
        return characterClassMapper.toResponse(updatedCharacterClass);
    }

    @Transactional
    public void deleteCharacterClass(Long id) {
        CharacterClass characterClass = characterClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));
        characterClassRepository.delete(characterClass);
    }
}

