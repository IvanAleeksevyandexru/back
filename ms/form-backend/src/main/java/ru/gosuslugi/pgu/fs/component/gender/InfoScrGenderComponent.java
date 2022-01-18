package ru.gosuslugi.pgu.fs.component.gender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.component.AbstractGenderComponent;

import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.GInfoScr;
import static ru.gosuslugi.pgu.dto.descriptor.types.ComponentType.InfoScr;

@Slf4j
@Component
@RequiredArgsConstructor
public class InfoScrGenderComponent extends AbstractGenderComponent<String> {

    @Override
    public ComponentType getType() {
        return GInfoScr;
    }


    @Override
    protected ComponentType getTargetComponentType() {
        return InfoScr;
    }
}
