package ru.gosuslugi.pgu.fs.common.descriptor.impl;

import lombok.val;
import ru.gosuslugi.pgu.dto.descriptor.FieldComponent;
import ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ru.gosuslugi.pgu.dto.descriptor.ServiceDescriptor.CLARIFICATIONS_ATTR;

/**
 * Конвертер описаний InfoScreen
 *
 * Вынесено в задаче EPGUCORE-48635
 * Данная необходимость возникла, т.к. сейчас на одно и то же модальное окно могут быть ссылки с разных экранов,
 * и они сейчас просто копируются во все эти экраны. Если нужно исправить текст, то приходится искать его во всех
 * копиях. И искать по всему json-у бывает проблематично. Кроме того, модальное окно может содержать очень много
 * данных, не которые их них не помещаются на один экран, а если учесть, что это часть основного экрана, то часто
 * весь компонент занимает больше страницы.
 */
final class DescriptorClarificationConverter {

    private DescriptorClarificationConverter() {}

    /**
     * Конвертация описанных в разделе списка InfoScreen.clarifications[] в clarification объекты
     * описанные в разделе clarifications JSON услуги
     * @param descriptor описание JSON услуги
     */
    public static void convert(ServiceDescriptor descriptor) {
        val clarifications = descriptor.getClarifications();
        descriptor.getApplicationFields()
                .forEach(it -> transformComponentClarifications(it, clarifications));
        descriptor.setClarifications(null);
    }

    /**
     * Трансформация атрибута clarifications компонента.
     * Список название в реальные объекты
     * @param fieldComponent - компонент экрана
     * @param clarifications - список модальных окон услуги
     */
    private static void transformComponentClarifications(FieldComponent fieldComponent, Map<String, Object> clarifications) {
        val attrs = fieldComponent.getAttrs();
        if (attrs == null) return;
        val clarificationList = fieldComponent.getAttrs().get(CLARIFICATIONS_ATTR);
        // Описание выполнено как часть компонента, трансформация не требуется
        if (clarificationList instanceof HashMap) return;
        if (clarificationList == null) return;

        val converted = new HashMap<String, Object>();
        fieldComponent.getAttrs().put(CLARIFICATIONS_ATTR, converted);
        if (clarifications == null) return;
        for (String key : (ArrayList<String>) clarificationList){
            val clarification = clarifications.get(key);
            if (clarification == null) continue;
            converted.put(key, clarification);
        }
    }
}
