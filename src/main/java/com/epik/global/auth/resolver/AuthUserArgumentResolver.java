package com.epik.global.auth.resolver;

import com.epik.global.auth.annotation.AuthUser;
import com.epik.global.exception.ErrorCode;
import com.epik.global.exception.custom.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {
    // 1. 어떤 파라미터를 이 리졸버가 처리할지 결정
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info(">>> AuthUserArgumentResolver.supportsParameter(MethodParameter) 호출");
        // 파라미터에 @AuthUser 어노테이션이 붙어 있으면 이 리졸버를 사용
        return parameter.hasParameterAnnotation(AuthUser.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        log.info(">>> AuthUserArgumentResolver.resolverArgument 호출");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 애노테이션 정보 가져오기
        AuthUser authUser = parameter.getParameterAnnotation(AuthUser.class);
        boolean isRequired = (authUser != null) && authUser.required();

        // 1. 인증되지 않은 사용자(=authentication 생성안되거나, anonymousUser로 되어있는 경우)
        if (isUnauthenticated(authentication)) {
            log.info(">>> 인증되지 않은 사용자 (Anonymous)");
            if (isRequired) {
                // 필수 로그인인데 없는 경우 -> 커스텀 예외로 401 응답 유도
                throw new BusinessException(ErrorCode.MEMBER_NOT_LOGGED_IN);
            }
            // 선택적 로그인인데 없는 경우 -> null 반환
            return null;
        }

        // 2. 인증된 사용자인 경우 (Long 타입 형변환)
        Long userId = (Long) authentication.getPrincipal();
        log.info(">>> 추출된 유저 ID : {}", userId);
        return userId;
    }

    // 가독성을 위한 헬퍼 메서드
    private boolean isUnauthenticated(Authentication auth) {
        return auth == null ||
                !auth.isAuthenticated() ||
                "anonymousUser".equals(auth.getPrincipal());
    }
}
