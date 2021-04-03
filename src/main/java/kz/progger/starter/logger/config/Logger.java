package kz.progger.starter.logger.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {

    boolean logArgValue() default true;

    int reqLogArgValLength() default -1;

    int resLogArgValLength() default -1;

}

