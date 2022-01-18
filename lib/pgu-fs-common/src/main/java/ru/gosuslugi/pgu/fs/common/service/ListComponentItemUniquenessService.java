package ru.gosuslugi.pgu.fs.common.service;

import ru.gosuslugi.pgu.dto.cycled.CycledApplicantAnswerItem;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;

import java.util.List;
import java.util.Map;

public interface ListComponentItemUniquenessService {

    boolean initUniqueKeys(FieldComponent fieldComponent, CycledApplicantAnswerItem answerItem);

    List<Boolean> removeUniquenessKeys(FieldComponent cycledComponent, CycledApplicantAnswerItem cycledAnswerItem);

    List<List<Map<String, String>>> validateCycledItemUniqueness(FieldComponent cycledComponent,
                                                                 List<CycledApplicantAnswerItem> answerItems,
                                                                 int indexOfCurrentAnswerItem);

    List<List<Map<String, String>>> validateCycledItemsUniqueness(FieldComponent cycledComponent,
                                                                  List<CycledApplicantAnswerItem> answerItems);

    List<List<Map<String, String>>> validateRepeatableFieldsItemsUniqueness(FieldComponent fieldComponent,
                                                                            List<Map<String, String>> itemsAnswers);

    void updateDisclaimerForUniquenessErrors(FieldComponent fieldComponent, List<List<Map<String, String>>> uniquenessErrors);

    }
