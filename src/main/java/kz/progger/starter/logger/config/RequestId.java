package kz.progger.starter.logger.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class RequestId {

    private static final DateTimeFormatter dateTimePartFormatter = DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss_SSS");

    public static String generateRequestId() {
        return LocalDateTime.now().format(dateTimePartFormatter)
                + "_" + RandomStringUtils.randomAlphanumeric(3);
    }

    public String get() {
        return MDC.get("reqId");
    }

    void set() {
        set(generateRequestId());
    }

    public void set(String reqId) {
        MDC.put("reqId", reqId);
    }

    void clear() {
        MDC.remove("reqId");
    }

}
