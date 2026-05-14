package com.eduardo.rpg.common;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unused")
public class RequirementMapperHelper {

    private RequirementMapperHelper() {
    }

    public static <R, T> List<T> mapRequirements(
        List<R> requests,
        String requirementLabel,
        Function<R, Long> requiredClassIdExtractor,
        Function<Long, CharacterClass> requiredClassResolver,
        BiFunction<R, CharacterClass, T> requirementFactory
    ) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> requirements = new ArrayList<>();
        Set<Long> classIds = new HashSet<>();

        for (int i = 0; i < requests.size(); i++) {
            R request = requests.get(i);
            if (request == null) {
                throw new IllegalArgumentException("Requisito de " + requirementLabel + " na posição " + i + " não pode ser nulo");
            }

            Long requiredClassId = requiredClassIdExtractor.apply(request);
            if (requiredClassId == null) {
                throw new IllegalArgumentException("Requisito de " + requirementLabel + " na posição " + i + " precisa informar a classe");
            }

            if (!classIds.add(requiredClassId)) {
                throw new IllegalArgumentException("A mesma classe não pode aparecer mais de uma vez nos requisitos");
            }

            CharacterClass requiredClass = requiredClassResolver.apply(requiredClassId);
            if (requiredClass == null) {
                throw new ResourceNotFoundException("Classe não encontrada!");
            }

            requirements.add(requirementFactory.apply(request, requiredClass));
        }

        return requirements;
    }
}



