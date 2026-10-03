package com.apr.community.global.requester;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 컨트롤러 파라미터에 요청자 id(String)를 주입: {@code @RequesterId String requester} */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequesterId {
}
