package com.haydenshui.stock.aspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

import com.haydenshui.stock.lib.annotation.Lock;
import com.haydenshui.stock.utils.RedissonUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
public class LockAspect {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint pjp, Lock lock) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();

        String[] paramNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();

        String rawKey = lock.lockKey();
        String resolvedKey = resolveKeyWithNesting(rawKey, paramNames, args);

        RLock rLock = RedissonUtils.lock(resolvedKey);
        boolean locked = false;

        try {
            locked = rLock.tryLock(lock.waitTime(), lock.leaseTime(), TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("Unable to acquire lock for key: " + resolvedKey);
            }

            return pjp.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while trying to acquire lock", e);
        } finally {
            if (locked && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    private String resolveKeyWithNesting(String rawKey, String[] paramNames, Object[] args) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(rawKey);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
<<<<<<< HEAD
            String placeholder = matcher.group(1);
=======
            String placeholder = matcher.group(1); // positionDTO.id
>>>>>>> 13c6d9d36c826dd91c3f04d952de90f7b349efbe
            String[] parts = placeholder.split("\\.");
            String paramName = parts[0];

            Object value = null;
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName)) {
                    value = args[i];
                    break;
                }
            }

            if (value != null && parts.length > 1) {
                BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);
                for (int i = 1; i < parts.length; i++) {
                    value = wrapper.getPropertyValue(parts[i]);
                    if (value == null) break;
                    wrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);
                }
            }

            matcher.appendReplacement(sb, value == null ? "null" : value.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
