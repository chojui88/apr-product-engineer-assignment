package com.apr.community.global.requester;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RequesterIdArgumentResolverTest {

    private final RequesterIdArgumentResolver resolver = new RequesterIdArgumentResolver();

    private Object resolve(MockHttpServletRequest request) {
        return resolver.resolveArgument(null, null, new ServletWebRequest(request), null);
    }

    @Test
    void 헤더가_있으면_헤더_값() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("x-user-id", "Jelin");
        req.setCookies(new Cookie("x-user-id", "cos_holic"));
        assertThat(resolve(req)).isEqualTo("Jelin");
    }

    @Test
    void 헤더가_없으면_쿠키_값() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("x-user-id", "cos_holic"));
        assertThat(resolve(req)).isEqualTo("cos_holic");
    }

    @Test
    void 헤더가_공백이면_쿠키로_넘어감() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("x-user-id", "   ");
        req.setCookies(new Cookie("x-user-id", "mintcream"));
        assertThat(resolve(req)).isEqualTo("mintcream");
    }

    @Test
    void 헤더_값의_앞뒤_공백은_제거() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("x-user-id", "  Jelin  ");
        assertThat(resolve(req)).isEqualTo("Jelin");
    }

    @Test
    void 둘_다_없으면_apr_tester() {
        assertThat(resolve(new MockHttpServletRequest())).isEqualTo("apr_tester");
    }

    @Test
    void 쿠키_값이_비어_있으면_apr_tester() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("x-user-id", ""));
        assertThat(resolve(req)).isEqualTo("apr_tester");
    }

    @Test
    void 쿠키_인코딩이_잘못되면_apr_tester() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("x-user-id", "%E0%A4%A"));
        assertThat(resolve(req)).isEqualTo("apr_tester");
    }

    @Test
    void 쿠키의_플러스는_공백이_아니라_플러스로_유지() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("x-user-id", "a+b"));
        assertThat(resolve(req)).isEqualTo("a+b");
    }

    @Test
    void 쿠키의_퍼센트_인코딩은_디코딩() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("x-user-id", "a%2Bb%20c"));   // encodeURIComponent("a+b c")
        assertThat(resolve(req)).isEqualTo("a+b c");
    }
}
