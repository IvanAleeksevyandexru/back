package ru.gosuslugi.pgu.fs.common.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.gosuslugi.pgu.fs.common.service.UserCookiesService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Фильтр для сохранение информации от таймзоне пользователя, которую передает фронт.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimezoneFilter extends OncePerRequestFilter {

    private static final String TIMEZONE_COOKIE = "timezone";
    private static final String DEFAULT_TIMEZONE = "+3";

    private final UserCookiesService userCookiesService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String timezone = Optional.ofNullable(request.getCookies())
                .map(List::of)
                .map(this::parseTimeZone)
                .orElse(DEFAULT_TIMEZONE);
        userCookiesService.setUserTimezone(timezone);
        filterChain.doFilter(request, response);
    }

    private String parseTimeZone(List<Cookie> cookies) {
        Optional<Cookie> cookieOptional = cookies
                .stream()
                .filter(cookie -> TIMEZONE_COOKIE.equals(cookie.getName()))
                .findFirst();

        if(cookieOptional.isEmpty()){
            return null;
        }

        try {
            int timezone = Integer.parseInt(cookieOptional.get().getValue());
            if (timezone >= -18 && timezone <= 18) {
                return timezone >= 0 ? "+" + timezone : String.valueOf(timezone);
            }
        } catch (NumberFormatException e) {
            log.warn("Incorrect timezone value = " + cookieOptional.get().getValue());
        }

        return null;
    }

}
