package kz.progger.starter.logger.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = LoggerInterceptor.class)
@ConditionalOnMissingBean({LoggerInterceptor.class, RequestListener.class})
@EnableConfigurationProperties({RequestId.class})
public class LoggerAutoConfiguration {

}
