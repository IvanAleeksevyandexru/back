package ru.gosuslugi.pgu.fs.service.custom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.ScenarioRequest;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public abstract class AbstractCustomScreenService implements CustomScreenService {

    @Autowired
    protected MainScreenServiceRegistry registry;

    @PostConstruct
    protected void register() {
        registry.register(this);
    }

    @Override
    public void saveCacheToDraft(String serviceId, ScenarioRequest request) {}
}
