package ru.gosuslugi.pgu.components.descriptor.attr_factory;

import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.gosuslugi.pgu.components.descriptor.ComponentField;
import ru.gosuslugi.pgu.components.descriptor.FieldGroup;
import ru.gosuslugi.pgu.components.descriptor.types.Action;
import ru.gosuslugi.pgu.dto.descriptor.CycledAttrs;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.placeholder.Placeholder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Создает сущности из атрибутов, прописанных в компоненте в {@code attrs}
 */
@RequiredArgsConstructor
public class FieldComponentAttrsFactory implements AttrsFactory {

    private final FieldComponent fieldComponent;

    /**
     * Достаёт рефы и складывает их в {@code Map}
     * @return рефы или пустая {@code Map} рефов
     */
    public Map<String, Object> getRefsMap() {
        Map<String, Object> attrs = Optional.ofNullable(fieldComponent.getAttrs()).orElseGet(() -> {
            fieldComponent.setAttrs(new HashMap<>());
            return fieldComponent.getAttrs();
        });
        return (Map<String, Object>) attrs.computeIfAbsent(REFS_ATTR, key -> new HashMap<String, Object>());
    }

    /**
     * Создает группы полей (поле {@code fieldGroups})
     * @return группы полей или пустой список группы полей
     */
    public List<FieldGroup> getComponentFieldGroups() {
        var fieldsList = Optional.ofNullable(fieldComponent.getAttrs()).orElse(new HashMap<>()).get("fieldGroups");
        if(fieldsList instanceof List) {
            return ((List<Map>) fieldsList).stream().map(el -> {
                FieldGroup fg = new FieldGroup();
                fg.setGroupName(el.getOrDefault("groupName", "").toString());
                fg.setFields(getComponentFields(el.get("fields")));
                fg.setAttrs(getCycledAttrs(el.get("attrs")));
                fg.setNeedDivider((Boolean) el.get("needDivider"));
                return fg;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Преобразует список из полей (поле {@code fieldGroups}) в первоначальный список List<Map<String, Object>>
     * @param hiddenEmptyGroups показывать или скрывать группы с полностью пустыми значениями
     * @param hiddenEmptyFields показывать или скрывать поля с пустым значением
     * @return список Map полей или пустой список Map полей
     */
    public List<Map<String, Object>> getComponentFieldGroupsMap(List<FieldGroup> fieldGroups, boolean hiddenEmptyGroups, boolean hiddenEmptyFields) {
        return fieldGroups.stream()
                .map(fieldGroup -> {
                    Map<String, Object> result = new HashMap<>();
                    if (fieldGroup.getAttrs() != null) {
                        result.put("attrs", fieldGroup.getAttrs());
                    }
                    List<Map<String, String>> fieldList = null;
                    if (!CollectionUtils.isEmpty(fieldGroup.getFields())) {
                        fieldList = getListOfFieldsMaps(fieldGroup.getFields(), hiddenEmptyFields);
                        result.put("fields", fieldList);
                    }
                    if (fieldGroup.getNeedDivider() != null) {
                        result.put("needDivider", fieldGroup.getNeedDivider());
                    }
                    result.put("groupName", fieldGroup.getGroupName());

                    if (hiddenEmptyGroups) {
                        boolean isEmptyGroup = Optional.ofNullable(fieldList).stream().flatMap(Collection::stream).map(map -> map.get("value")).allMatch(Objects::isNull);
                        if (isEmptyGroup) {
                            return Collections.<String, Object>emptyMap();
                        }
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Создает атрибуты цикличных компонентов из передаваемой мапы с ними
     * @param attrs атрибуты цикличных компонентов
     * @return атрибуты цикличных компонентов, атрибуты не могут быть {@code null}, значения атрибутов - могут быть {@code null}
     */
    public CycledAttrs getCycledAttrs(Object attrs) {
        CycledAttrs cycledAttrs = new CycledAttrs();
        if(attrs instanceof Map) {
            var attrsMap = (Map<String, Object>)attrs;
            cycledAttrs.setCycledAnswerId((String)attrsMap.get("cycledAnswerId"));
            cycledAttrs.setCycledAnswerIndex((String)attrsMap.get("cycledAnswerIndex"));
        }
        return cycledAttrs;
    }

    public String getPathToArray(Object attrs) {
        if (attrs instanceof Map) {
            var attrsMap = (Map<String, Object>) attrs;
            return (String) attrsMap.get("pathToArray");
        }
        return null;
    }

    /**
     * Создает поля компонента
     * @param fieldList список полей в компоненте
     * @return поля компонента или пустой список полей компонента
     */
    public List<ComponentField> getComponentFields(Object fieldList) {
        if(fieldList instanceof List) {
            return ((List<Map<String, String>>) fieldList).stream()
                    .map(el -> new ComponentField(el.get("label"), el.get("value"), el.get("fieldName"), getRank(el.get("rank"))))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Boolean getRank (Object rank) {
        if (rank == null) {
            return Boolean.FALSE;
        }
        if (rank instanceof String) {
            return Boolean.valueOf((String) rank);
        }
        if (rank instanceof Boolean) {
            return (Boolean) rank;
        }
        return Boolean.FALSE;
    }

    /**
     * Создает аналогичный первоначальному List<Map<String, String> из List<ComponentField>
     * @param fieldList список полей в компоненте
     * @param hiddenEmptyFields показывать или скрывать поля с пустым значением
     * @return List<Map<String, String> или пустой список
     */
    public List<Map<String, String>> getListOfFieldsMaps(Object fieldList, boolean hiddenEmptyFields) {
        if(fieldList instanceof List) {
            return ((List<ComponentField>) fieldList).stream()
                    .map(componentField -> {
                        Map<String, String> result = new HashMap<>();
                        if (StringUtils.hasText(componentField.getFieldName())) {
                            result.put("fieldName", componentField.getFieldName());
                        }
                        String label = componentField.getLabel();
                        String value = componentField.getValue();
                        if ("null".equals(label)) {
                            label = null;
                        }
                        if ("null".equals(value)) {
                            value = null;
                        }
                        boolean isLabelHasText = StringUtils.hasText(label);
                        boolean isValueHasText = StringUtils.hasText(value);
                        if (componentField.getRank() != null) {
                            result.put("rank", componentField.getRank().toString());
                        }

                        if (!isLabelHasText && !isValueHasText || !isValueHasText && hiddenEmptyFields) {
                            return Collections.<String, String>emptyMap(); // полностью отсутствующий field FE не выводит на экран либо нет только значения и выставлен флаг скрытия таких значений
                        }
                        if (!isValueHasText) {
                            value = null; // null в value FE превращает в дефис
                        } else if (!isLabelHasText) {
                            label = null; // null в label требуется установить, чтобы FE вывел value без label
                        }
                        result.put("label", label);
                        result.put("value", value);

                        return result;
                    }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Получает аргументы компонента
     * @return аргументы компонента
     */
    public Map<String, String> getComponentArguments() {
        return Optional.ofNullable(fieldComponent.getArguments()).orElse(new HashMap<>());
    }

    /**
     * Создаются плайсхолдеры
     * @return плейсхолдеры
     */
    public List<Placeholder> getPlaceholders() {
        var extraAttrsMap = Optional.ofNullable(fieldComponent.getAttrs()).orElse(new HashMap<>()).get("placeholders");
        if (extraAttrsMap instanceof Map) {
            return getPlaceholders((Map<String, String>) extraAttrsMap);
        }

        return Collections.emptyList();
    }

    /**
     * Создает {@link Action} из указанной мапы
     * @param actionMap мапа с атрибутами action-а
     * @return новый action
     */
    public Action createAction(Map<String, Object> actionMap) {
        Action action = new Action();
        action.setLabel((String)actionMap.get("label"));
        action.setValue((String)actionMap.get("value"));
        action.setType((String)actionMap.get("type"));
        action.setAction((String)actionMap.get("action"));
        action.setAttrs(getCycledAttrs(actionMap.get("attrs")));
        action.setPathToArray(getPathToArray(actionMap.get("attrs")));
        return action;
    }

    public boolean getBooleanAttr(String booleanAttrName, boolean defaultValue) {
        return fieldComponent.getBooleanAttr(booleanAttrName, defaultValue);
    }
}
