package com.eduardo.rpg.security;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserRepository userRepository;

    public User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Acesso não autenticado");
        }

        return userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public boolean isMasterOrAdmin(User user) {
        return user != null && (user.getRole() == Role.MASTER || user.getRole() == Role.ADMIN);
    }

    public void requireMasterOrAdmin(User user) {
        if (!isMasterOrAdmin(user)) {
            throw new AccessDeniedException("Somente MASTER ou ADMIN podem acessar este recurso");
        }
    }

    public void requireSameUserOrAdmin(User user, Long targetUserId) {
        if (user == null || targetUserId == null || (!isAdmin(user) && !user.getId().equals(targetUserId))) {
            throw new AccessDeniedException("Sem permissão para acessar este usuário");
        }
    }

    public void requireCampaignOwnerOrAdmin(User user, Campaign campaign) {
        if (user == null || campaign == null || campaign.getMaster() == null) {
            throw new AccessDeniedException("Sem permissão para acessar esta campanha");
        }

        if (!isAdmin(user) && !campaign.getMaster().getId().equals(user.getId())) {
            throw new AccessDeniedException("Sem permissão para acessar esta campanha");
        }
    }

    public void requireCharacterOwnerCampaignOrAdmin(User user, Character character) {
        if (user == null || character == null) {
            throw new AccessDeniedException("Sem permissão para acessar este personagem");
        }

        boolean ownsCharacter = character.getUser() != null && user.getId().equals(character.getUser().getId());
        boolean ownsCampaign = character.getCampaign() != null
            && character.getCampaign().getMaster() != null
            && user.getId().equals(character.getCampaign().getMaster().getId());

        if (!isAdmin(user) && !ownsCharacter && !ownsCampaign) {
            throw new AccessDeniedException("Sem permissão para acessar este personagem");
        }
    }

    public void requireCharacterCreationPermission(User user, Long targetUserId, CharacterRole role) {
        requireSameUserOrAdmin(user, targetUserId);

        if (!isAdmin(user) && user.getRole() == Role.PLAYER && role == CharacterRole.MONSTER) {
            throw new AccessDeniedException("Player não pode criar monstro");
        }
    }
}

