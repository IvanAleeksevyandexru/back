package ru.gosuslugi.pgu.fs.service.ratelimit.impl;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.gosuslugi.pgu.fs.service.ratelimit.RateLimitService;

@Slf4j
@NoArgsConstructor
public class RateLimitServiceStub implements RateLimitService {

    @Override
    public void apiCheck(String key) {
        log.debug("RateLimitServiceStub userKey: " + key);
    }
}