package ru.gosuslugi.pgu.fs.common.variable;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public abstract class AbstractVariable implements Variable {

    @Autowired
    protected VariableRegistry variableRegistry;

    @PostConstruct
    protected void register() {
        variableRegistry.register(this);
    }

}
