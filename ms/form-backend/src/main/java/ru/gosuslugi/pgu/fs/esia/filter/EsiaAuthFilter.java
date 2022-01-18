package ru.gosuslugi.pgu.fs.esia.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.atc.carcass.security.model.EsiaOAuthTokenSession;
import ru.atc.carcass.security.rest.model.EsiaResponse;
import ru.atc.carcass.security.rest.model.EsiaUserId;
import ru.atc.carcass.security.rest.model.orgs.AttorneisCollection;
import ru.atc.carcass.security.rest.model.orgs.OrgsContainer;
import ru.atc.carcass.security.rest.model.person.PersonContainer;
import ru.atc.carcass.security.service.impl.EsiaRestClientServiceImpl;
import ru.atc.carcass.security.service.impl.OAuthTokenUtil;
import ru.atc.carcass.security.service.impl.SystemAutorityUtils;
import ru.atc.carcass.security.service.impl.ThreadLocalTokensContainerManagerService;
import ru.gosuslugi.pgu.fs.common.exception.FormBaseException;
import ru.gosuslugi.pgu.common.core.exception.dto.error.ErrorMessage;
import ru.gosuslugi.pgu.common.core.exception.dto.error.ErrorMessageWithoutModal;
import ru.gosuslugi.pgu.common.esia.search.dto.UserOrgData;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.common.logging.service.SpanService;
import ru.gosuslugi.pgu.common.core.json.JsonProcessingUtil;
import ru.gosuslugi.pgu.fs.config.properties.EsiaServiceProperties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.isEmpty;
import static ru.gosuslugi.pgu.common.logging.service.SpanService.*;

/**
 * Фильтр для проверки авторизационных данных на основе токена ЕСИА. Также запрашивает персональные данные и помещает их
 * в userPersonalData (bean со скоупом request).
 */
@Slf4j
@Order(1)
@Component
public class EsiaAuthFilter extends OncePerRequestFilter {

    private static final String ACC_T_COOKIE = "acc_t";

    private final EsiaRestClientServiceImpl esiaRestClientService;
    private final EsiaServiceProperties esiaServiceProperties;
    private final ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService;
    private final SpanService spanService;
    private final UserPersonalData userPersonalData;
    private final UserOrgData userOrgData;

    @Value("${esia.auth.exclude-urls:#{null}}")
    private List<String> excludeUrls;

    @Value("${esia.auth.enabled:#{true}}")
    private boolean esiaAuthEnabled;

    public EsiaAuthFilter(EsiaRestClientServiceImpl esiaRestClientService,
                          EsiaServiceProperties esiaServiceProperties,
                          ThreadLocalTokensContainerManagerService threadLocalTokensContainerManagerService,
                          SpanService spanService,
                          UserPersonalData userPersonalData,
                          UserOrgData userOrgData) {
        this.esiaRestClientService = esiaRestClientService;
        this.esiaServiceProperties = esiaServiceProperties;
        this.threadLocalTokensContainerManagerService = threadLocalTokensContainerManagerService;
        this.spanService = spanService;
        this.userPersonalData = userPersonalData;
        this.userOrgData = userOrgData;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if(!esiaAuthEnabled){
            spanService.addTagToSpan(USER_ID_TAG, "Unauthorized");
            return true;
        }
        if (excludeUrls != null) {
            for (String excludeUrl : excludeUrls) {
                if (request.getRequestURI().startsWith(excludeUrl)) {
                    spanService.addTagToSpan(USER_ID_TAG, "Unauthorized");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        List<Cookie> cookies = Optional.ofNullable(request.getCookies())
                .map(List::of)
                .orElse(Collections.emptyList());
        String token = parseToken(cookies);
        if (isEmpty(token)) {
            setErrorResponse(UNAUTHORIZED, response, "Cookie '" + ACC_T_COOKIE + "' is not found. ESIA authorization failed.",
                    true,null);
            return;
        }

        try {
            EsiaOAuthTokenSession tokenInfo = OAuthTokenUtil.getTokenInfo(token);
            EsiaUserId esiaUserId = new EsiaUserId(tokenInfo.getUserId(), tokenInfo.getOrgOid(), null);
            threadLocalTokensContainerManagerService.put(esiaUserId, token);

            long startTs = System.currentTimeMillis();
            EsiaResponse esiaResponse = spanService.runExternalService(
                    "esiaRestClientService: get all person data",
                    "esia.request.getPersonAll",
                    () -> esiaRestClientService.getPerson(esiaUserId,
                            Set.of("documents", "documents.elements", "kids", "addresses", "contacts", "contacts.elements")),
                    Map.of("esiaUserId", String.valueOf(esiaUserId),
                            "url", esiaServiceProperties.getCalculatedUrl())
            );
            if (log.isDebugEnabled()) log.debug("Response from ESIA ({}ms): {}", System.currentTimeMillis() - startTs, esiaResponse);
            if (esiaResponse == null || !"ESIA-OK".equals(esiaResponse.getError())) {
                setErrorResponse(UNAUTHORIZED, response, "User not authorized", false, null);
                return;
            }
            if (!(esiaResponse instanceof PersonContainer)) {
                setErrorResponse(BAD_REQUEST, response, "Response from ESIA in incorrect format. Received class: "
                        + esiaResponse.getClass().getCanonicalName() + ": " + esiaResponse, true, null);
                return;
            }
            PersonContainer personContainer = (PersonContainer) esiaResponse;
            userPersonalData.update(personContainer, tokenInfo);
            if (userPersonalData.getOrgId() != null) {
                EsiaResponse orgEsiaResponse = spanService.runExternalService(
                        "esiaRestClientService: get all Org data",
                        "esia.request.getOrgAll",
                        () -> esiaRestClientService.getOrg(esiaUserId,
                                Set.of("contacts", "contacts.elements",
                                        "addresses", "addresses.elements",
                                        "vehicles", "vehicles.elements",
                                        "approve", "approve.elements",
                                        "employees", "employees.elements",
                                        "roles", "roles.elements")),
                        Map.of("esiaUserId", String.valueOf(esiaUserId),
                                "url", esiaServiceProperties.getCalculatedUrl())
                );
                AttorneisCollection attorneisCollection = spanService.runExternalService(
                        "esiaRestClientService: get Employee Attornies",
                        "esia.request.getEmployeeAttornies",
                        () -> esiaRestClientService.getEmployeeAttornies(esiaUserId),
                        Map.of("esiaUserId", String.valueOf(esiaUserId),
                                "url", esiaServiceProperties.getCalculatedUrl())
                );
                String systemAuthority = SystemAutorityUtils.getSystemAuthority(((OrgsContainer)orgEsiaResponse).getGroups(), ((OrgsContainer)orgEsiaResponse).getPerson().isChief());
                userOrgData.update((OrgsContainer) orgEsiaResponse, systemAuthority, attorneisCollection.getEsiaAttorneis());
            }
            spanService.addTagToSpan(USER_ID_TAG, String.valueOf(esiaUserId.getUserId()));
        } catch (ru.atc.carcass.security.exception.SecurityException e) {
            setErrorResponse(UNAUTHORIZED, response, "User not authorized", false, e);
            return;
        } catch (Exception e) {
            setErrorResponse(BAD_REQUEST, response, "Error on request person info from ESIA", true, e);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, String message, boolean isLogError, Throwable ex) {
        if (isLogError) {
            log.error(String.format("Failed to get user personal data form ESIA(%s). %s", esiaServiceProperties.getCalculatedUrl(), message), ex);
        } else if (log.isInfoEnabled()) {
            log.info(message);
        }
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {
            ErrorMessage err = new ErrorMessageWithoutModal();
            err.setStatus(status.name());
            err.setMessage(message);
            err.setDescription(ex != null ? ex.getMessage() : null);
            err.setTraceId(spanService.getCurrentTraceId());

            String json = JsonProcessingUtil.toJson(err);
            if (ex != null) {
                spanService.addTagToSpan(EXCEPTION_TAG, ex.getClass().getName());
            }
            spanService.addTagToSpan(MESSAGE_TAG, err.toString());
            response.getWriter().write(json);
        } catch (IOException e) {
            throw new FormBaseException(e.getMessage(), e);
        }
    }

    private String parseToken(List<Cookie> authorizationToken) {
        Optional<Cookie> cookieOptional = authorizationToken
                .stream()
                .filter(cookie -> ACC_T_COOKIE.equals(cookie.getName()))
                .findFirst();

        if(cookieOptional.isEmpty()){
            return null;
        }
        return cookieOptional.get().getValue();
    }


}
