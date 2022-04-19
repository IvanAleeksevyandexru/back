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
 * Фильтр для сохранение выбранного региона пользователя, которую передает фронт.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSelectedRegionFilter extends OncePerRequestFilter {

    private static final String USER_SELECTED_REGION_COOKIE = "userSelectedRegion";
    private static final String DEFAULT_USER_SELECTED_REGION = "00000000000";

    private final UserCookiesService userCookiesService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String region = Optional.ofNullable(request.getCookies())
                .map(List::of)
                .map(this::getUserSelectedRegion)
                .orElse(DEFAULT_USER_SELECTED_REGION);
        userCookiesService.setUserSelectedRegion(region);
        filterChain.doFilter(request, response);
    }

    private String getUserSelectedRegion(List<Cookie> cookies) {
        return cookies
                .stream()
                .filter(cookie -> USER_SELECTED_REGION_COOKIE.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(DEFAULT_USER_SELECTED_REGION);
    }

}
