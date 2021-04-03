package kz.progger.starter.logger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
@Order(1000)
@Aspect
@Component
public class LoggerInterceptor {
    private ObjectMapper objectMapper;
    private ThreadLocal<ArrayDeque<String>> callStackLocal = new ThreadLocal<>();
    @Autowired
    public LoggerInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("execution(* *(..)) && @annotation(logger)")
    protected Object aroundMethodInvoke(ProceedingJoinPoint joinPoint, Logger logger) throws Throwable {
        return logger(logger, joinPoint);
    }

    @Around("execution(* *(..)) && @within(logger) " +
            "&& !@annotation(kz.progger.starter.logger.config.ExcludeLogger)"
            + " && !@annotation(kz.progger.starter.logger.config.Logger)")
    protected Object aroundObjectInvoke(ProceedingJoinPoint joinPoint,  Logger logger) throws Throwable {
        return logger(logger, joinPoint);
    }

    public static String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "none";
    }

    private Object logger(Logger logger, ProceedingJoinPoint joinPoint) throws Throwable {
        String calledAs = getAuthenticatedUser();
        String joinPointName = StringUtils.substringAfterLast(joinPoint.getSignature()
                .getDeclaringTypeName(), ".")
                .concat(".")
                .concat(joinPoint.getSignature().getName());
        if (logger.logArgValue()) {
            if (logger.reqLogArgValLength() == -1) {
                log.info(
                        "invoke [{}] as {} with args: {}",
                        joinPointName,
                        calledAs,
                        objectMapper.writeValueAsString(joinPoint.getArgs())
                );
            } else {
                log.info(
                        "invoke [{}] as {} with args: {}",
                        joinPointName,
                        calledAs,
                        logger.reqLogArgValLength() == -1 ? objectMapper.writeValueAsString(joinPoint.getArgs())
                                : objectMapper.writeValueAsString(joinPoint.getArgs()).substring(0, logger.reqLogArgValLength()).concat("}]")
                );
            }
        } else {
            log.info(
                    "invoke [{}] as {} ",
                    joinPointName,
                    calledAs
            );
        }


        ArrayDeque<String> callStack = callStackLocal.get();
        if (callStack == null) {
            callStack = new ArrayDeque<>();
            callStackLocal.set(callStack);
        }
        callStack.addLast(joinPointName);

        long startAt = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long timeNanos = System.nanoTime() - startAt;

            if (logger.logArgValue()) {
                log.info(
                        "invoke [{}] ok in [{} ms] as {} with result: {}",
                        joinPointName,
                        TimeUnit.NANOSECONDS.toMillis(timeNanos),
                        calledAs,
                        logger.resLogArgValLength() == -1 ? objectMapper.writeValueAsString(result)
                                : objectMapper.writeValueAsString(result).substring(0, logger.resLogArgValLength()).concat("}]")
                );
            } else {
                log.info(
                        "invoke [{}] ok in [{} ms] as {}",
                        joinPointName,
                        TimeUnit.NANOSECONDS.toMillis(timeNanos),
                        calledAs
                );
            }

            return result;
        } catch (Throwable t) {
            long timeNanos = System.nanoTime() - startAt;
            log.error(
                    "invoke [{}] err in [{} ms] as {} with error: {} - {}",
                    joinPointName,
                    TimeUnit.NANOSECONDS.toMillis(timeNanos),
                    calledAs,
                    t.getClass().getSimpleName(),
                    t.getMessage()
            );
            throw t;
        } finally {
            callStack.removeLast();
            if (callStack.isEmpty()) {
                callStackLocal.remove();
            }
        }
    }

}
