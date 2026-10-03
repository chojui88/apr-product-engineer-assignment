package com.apr.community.global.requester;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class RequesterIdArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String HEADER_NAME = "x-user-id";
    public static final String COOKIE_NAME = "x-user-id";
    public static final String DEFAULT_USER = "apr_tester";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequesterId.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // TODO(#3) 헤더 > 쿠키 > DEFAULT_USER (빈 문자열은 없는 것으로 취급)
        throw new UnsupportedOperationException("TODO(#3)");
    }
}
