package ru.gosuslugi.pgu.fs.component.matcapital;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gosuslugi.pgu.dto.descriptor.types.ComponentType;
import ru.gosuslugi.pgu.fs.common.component.AbstractComponent;

@Component
@RequiredArgsConstructor
public class MatPeriodComponent extends AbstractComponent<String> {


    @Override
    public ComponentType getType() {
        return ComponentType.MatPeriod;
    }

}
