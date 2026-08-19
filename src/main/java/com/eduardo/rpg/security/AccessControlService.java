package com.eduardo.rpg.security;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
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
    private final CampaignRepository campaignRepository;

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

    public boolean isCampaignMaster(User user, Campaign campaign) {
        return user != null && campaign != null && campaign.getMaster() != null && campaign.getMaster().getId().equals(user.getId());
    }

    public boolean isCampaignPlayer(User user, Campaign campaign) {
        if (user == null || campaign == null || campaign.getPlayers() == null) return false;
        return campaign.getPlayers().stream().anyMatch(p -> p.getId().equals(user.getId()));
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

    public void requireCampaignViewPermission(User user, Campaign campaign) {
        if (user == null || campaign == null) {
            throw new AccessDeniedException("Sem permissão para acessar esta campanha");
        }

        if (isAdmin(user)) return;
        if (isCampaignMaster(user, campaign)) return;
        if (isCampaignPlayer(user, campaign)) return;

        throw new AccessDeniedException("Sem permissão para acessar esta campanha");
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

    // Game-content (races, classes, abilities, equipment) is either global
    // (campaignId == null, curated by ADMIN only) or scoped to one campaign
    // (created/managed by that campaign's own master, or ADMIN). Used for
    // create (campaignId comes from the request body) and for
    // update/delete (campaignId comes from the entity already on file).
    public void requireGameContentWritePermission(User user, Long campaignId) {
        if (isAdmin(user)) return;

        if (campaignId == null) {
            throw new AccessDeniedException("Apenas ADMIN pode gerenciar conteúdo global");
        }

        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));

        if (!isCampaignMaster(user, campaign)) {
            throw new AccessDeniedException("Apenas o mestre da campanha pode gerenciar este conteúdo");
        }
    }

    public void requireCharacterCreationPermission(User user, Long targetUserId, CharacterRole role) {
        requireSameUserOrAdmin(user, targetUserId);

        if (!isAdmin(user) && user.getRole() == Role.PLAYER && role == CharacterRole.MONSTER) {
            throw new AccessDeniedException("Player não pode criar monstro");
        }
    }
}

