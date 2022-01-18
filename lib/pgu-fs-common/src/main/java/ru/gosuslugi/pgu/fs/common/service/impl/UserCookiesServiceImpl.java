package ru.gosuslugi.pgu.fs.common.service.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;

/**
 * {@inheritDoc}
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
public class UserCookiesServiceImpl implements UserCookiesService {

    private String timezone;
    private String userSelectedRegion;

    @Override
    public String getUserTimezone() {
        return timezone;
    }

    @Override
    public void setUserTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String getUserSelectedRegion() {
        return userSelectedRegion;
    }

    @Override
    public void setUserSelectedRegion(String region) {
        this.userSelectedRegion = region;
    }
}
