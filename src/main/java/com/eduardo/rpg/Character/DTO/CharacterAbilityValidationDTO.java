package com.eduardo.rpg.Character.DTO;

import java.util.List;

public record CharacterAbilityValidationDTO(
    Long characterId,
    Long abilityId,
    String abilityName,
    Boolean canUse,
    List<RequirementCheckDTO> requirementChecks
) {
    public record RequirementCheckDTO(
        String requiredClass,
        Boolean meetsLevelRequirement,
        Integer requiredLevel,
        Integer characterLevel,
        Boolean meetsStrengthRequirement,
        Integer requiredStrength,
        Integer characterStrength,
        Boolean meetsDexterityRequirement,
        Integer requiredDexterity,
        Integer characterDexterity,
        Boolean meetsConstitutionRequirement,
        Integer requiredConstitution,
        Integer characterConstitution,
        Boolean meetsIntelligenceRequirement,
        Integer requiredIntelligence,
        Integer characterIntelligence,
        Boolean meetsWisdomRequirement,
        Integer requiredWisdom,
        Integer characterWisdom,
        Boolean meetsCharismaRequirement,
        Integer requiredCharisma,
        Integer characterCharisma,
        Boolean canUseForThisClass
    ) {}
}

