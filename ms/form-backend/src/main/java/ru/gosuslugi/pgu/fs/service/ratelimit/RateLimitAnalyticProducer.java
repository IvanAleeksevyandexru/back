package ru.gosuslugi.pgu.fs.service.ratelimit;

import ru.gosuslugi.pgu.dto.ratelimit.RateLimitOverHeadDto;

public interface RateLimitAnalyticProducer {

    void send(RateLimitOverHeadDto rateLimitOverHeadDto);
}
