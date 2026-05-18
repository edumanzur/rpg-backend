package com.eduardo.rpg.Character.DTO;

import java.util.List;

public record CharacterEquipmentValidationDTO(
    Long characterId,
    Long equipmentId,
    String equipmentName,
    Boolean canEquip,
    List<RequirementCheckDTO> requirementChecks
) {
    public record RequirementCheckDTO(
        String requiredClass,
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
        Boolean canEquipForThisClass
    ) {}
}

