package ru.gosuslugi.pgu.fs.helper;

import org.springframework.beans.factory.annotation.Autowired;
import ru.gosuslugi.pgu.fs.common.helper.HelperScreenRegistry;
import ru.gosuslugi.pgu.fs.common.helper.ScreenHelper;

import javax.annotation.PostConstruct;

public abstract class AbstractScreenHelper implements ScreenHelper {

    @Autowired
    protected HelperScreenRegistry screenRegistry;

    @PostConstruct
    protected void register() {
        screenRegistry.register(this);
    }

}
