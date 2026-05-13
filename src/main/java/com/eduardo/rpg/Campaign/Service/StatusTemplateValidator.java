package com.eduardo.rpg.Campaign.Service;

import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class StatusTemplateValidator {

    public void validateTemplates(List<StatusTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }

        Set<String> names = new HashSet<>();
        for (StatusTemplate template : templates) {
            if (template == null || template.getName() == null || template.getName().isBlank()) {
                throw new IllegalArgumentException("Todo status precisa ter um nome");
            }

            validateTemplateBounds(template);

            String normalized = template.getName().trim().toLowerCase();
            if (!names.add(normalized)) {
                throw new IllegalArgumentException("Não é permitido repetir o nome de status dentro da mesma campanha");
            }
        }
    }

    public void validateTemplateBounds(StatusTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("StatusTemplate inválido");
        }

        if (template.getMinValue() != null && template.getMaxValue() != null && template.getMinValue() > template.getMaxValue()) {
            throw new IllegalArgumentException("O valor mínimo não pode ser maior que o valor máximo");
        }

        if (template.getMinValue() != null && template.getDefaultValue() != null && template.getDefaultValue() < template.getMinValue()) {
            throw new IllegalArgumentException("O defaultValue precisa ser maior ou igual ao valor mínimo");
        }

        if (template.getMaxValue() != null && template.getDefaultValue() != null && template.getDefaultValue() > template.getMaxValue()) {
            throw new IllegalArgumentException("O defaultValue precisa ser menor ou igual ao valor máximo");
        }
    }
}


