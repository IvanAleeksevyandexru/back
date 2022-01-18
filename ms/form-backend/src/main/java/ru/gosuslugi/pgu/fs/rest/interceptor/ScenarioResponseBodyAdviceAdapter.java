package ru.gosuslugi.pgu.fs.rest.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import ru.gosuslugi.pgu.dto.ScenarioResponse;
import ru.gosuslugi.pgu.fs.service.impl.AdditionalAttributesHelper;

@ControllerAdvice
@RequiredArgsConstructor
public class ScenarioResponseBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    private final AdditionalAttributesHelper additionalAttributesHelper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    //TODO функционал временно отключен, т.к. additional используется для дилириумных услуг
    //оно затирается при getNextStep (и данные теряются)
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
//        if (body instanceof ScenarioResponse) {
//            ScenarioResponse scenarioResponse = (ScenarioResponse) body;
//            if (scenarioResponse.getScenarioDto() != null) {
//                additionalAttributesHelper.cleanUpAdditionalParameters(scenarioResponse.getScenarioDto());
//            }
//        }
        return body;
    }
}
