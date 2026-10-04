package com.apr.community.global.requester;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 요청자 판별: x-user-id 헤더 > x-user-id 쿠키 > 기본값 apr_tester
 * (로그인 없음. 빈 문자열·공백·잘못 인코딩된 쿠키는 값이 없는 것으로 취급)
 */
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
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return DEFAULT_USER;
        }

        // 1) 헤더 우선 (API 직접 호출, 서버 컴포넌트에서 전달)
        String fromHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(fromHeader)) {
            return fromHeader.trim();
        }

        // 2) 쿠키 (브라우저 호출, /dev-user 화면이 설정)
        String fromCookie = readCookie(request);
        if (StringUtils.hasText(fromCookie)) {
            return fromCookie.trim();
        }

        // 3) 둘 다 없으면 기본 요청자
        return DEFAULT_USER;
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (!StringUtils.hasText(value)) {
                    return null;
                }
                try {
                    // dev-user 화면은 encodeURIComponent 로 저장 → '+'는 원래 문자이므로 보존
                    return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    // 잘못된 인코딩의 쿠키는 무시하고 기본 요청자 사용
                    return null;
                }
            }
        }
        return null;
    }
}
