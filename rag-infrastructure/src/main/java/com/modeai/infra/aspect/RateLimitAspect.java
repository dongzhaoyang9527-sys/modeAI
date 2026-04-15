package com.modeai.infra.aspect;

import com.modeai.common.dto.Result;
import com.modeai.common.exception.BusinessException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("@annotation(com.modeai.infra.annotation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.modeai.infra.annotation.RateLimit rateLimit = method.getAnnotation(com.modeai.infra.annotation.RateLimit.class);

        String limiterName = rateLimit.value();
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(limiterName);

        if (rateLimiter.acquirePermission()) {
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded: {}", limiterName);
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }
    }
}
