package com.eduardo.rpg.Character.Service;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.DTO.CharacterMapper;
import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final CharacterMapper characterMapper;

    @Transactional
    public CharacterResponseDTO createCharacter(Long userId, CreateCharacterRequest dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        if (characterRepository.existsByNameAndUserId(dto.name(), userId)) {
            throw new IllegalArgumentException("Já existe um personagem com este nome para este usuário");
        }

        Character character = characterMapper.toEntity(dto);
        character.setUser(user);

        Character savedCharacter = characterRepository.save(character);
        return characterMapper.toResponse(savedCharacter);
    }

    @Transactional(readOnly = true)
    public CharacterResponseDTO findCharacterById(Long id) {
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        return characterMapper.toResponse(character);
    }

    @Transactional(readOnly = true)
    public List<CharacterResponseDTO> findCharactersByUserId(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        return characterRepository.findByUserId(userId)
            .stream()
            .map(characterMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CharacterResponseDTO> findCharactersByCampaignId(Long campaignId) {
        return characterRepository.findByCampaignId(campaignId)
            .stream()
            .map(characterMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CharacterResponseDTO> findAllCharacters() {
        return characterRepository.findAll()
            .stream()
            .map(characterMapper::toResponse)
            .toList();
    }

    @Transactional
    public CharacterResponseDTO updateCharacter(Long id, UpdateCharacterRequest dto) {
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));

        character = characterMapper.toEntity(dto, character);
        Character updatedCharacter = characterRepository.save(character);

        return characterMapper.toResponse(updatedCharacter);
    }

    @Transactional
    public void deleteCharacter(Long id) {
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        characterRepository.delete(character);
    }
}

