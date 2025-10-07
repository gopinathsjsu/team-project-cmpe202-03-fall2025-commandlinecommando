package com.commandlinecommandos.campusmarketplace.security;

import com.github.bucket4j.Bucket;
import com.commandlinecommandos.campusmarketplace.config.RateLimitingConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitingAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingAspect.class);
    
    @Autowired
    private Map<String, Bucket> rateLimitBuckets;

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) && " +
            "(execution(* com.commandlinecommandos.campusmarketplace.controller.AuthController.login(..)) || " +
            "execution(* com.commandlinecommandos.campusmarketplace.controller.AuthController.register(..)) || " +
            "execution(* com.commandlinecommandos.campusmarketplace.controller.AuthController.refreshToken(..)))")
    public Object rateLimitAuthEndpoints(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String clientIp = getClientIpAddress(request);
        String bucketKey = "auth_" + clientIp;

        Bucket bucket = rateLimitBuckets.computeIfAbsent(bucketKey, k -> RateLimitingConfig.createAuthRateLimiter());

        if (bucket.tryConsume(1)) {
            logger.debug("Rate limit check passed for IP: {}", clientIp);
            return joinPoint.proceed();
        } else {
            logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, request.getRequestURI());
            return createRateLimitResponse();
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private ResponseEntity<Map<String, String>> createRateLimitResponse() {
        Map<String, String> error = Map.of(
            "error", "Rate limit exceeded",
            "message", "Too many requests. Please try again later.",
            "retryAfter", "60"
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
}
