package kz.progger.starter.logger.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

@Component
public class RequestListener implements ServletRequestListener {

    private RequestId requestId;
    public RequestListener(RequestId requestId) {
        this.requestId = requestId;
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        requestId.clear();
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();

        String headerRequestId = request.getHeader("X-Request-Id");
        if (StringUtils.isNotBlank(headerRequestId)) {
            requestId.set(headerRequestId);
        } else {
            requestId.set();
        }
    }
}

