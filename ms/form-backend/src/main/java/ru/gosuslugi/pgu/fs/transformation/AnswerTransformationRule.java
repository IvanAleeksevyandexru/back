package ru.gosuslugi.pgu.fs.transformation;

import lombok.Data;
import ru.gosuslugi.pgu.dto.descriptor.RuleCondition;

import java.util.Set;

@Data
public class AnswerTransformationRule {
    private Set<RuleCondition> conditions;
    private Object transformation;
}
