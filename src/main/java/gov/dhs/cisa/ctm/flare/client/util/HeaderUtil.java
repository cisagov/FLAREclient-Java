package gov.dhs.cisa.ctm.flare.client.util;

import org.mitre.taxii.messages.xml11.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 */
public final class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private static final String APPLICATION_NAME = "flareclientApp";

    private HeaderUtil() {
    }

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-alert", message);
        headers.add("X-" + APPLICATION_NAME + "-params", param);
        return headers;
    }

    public static HttpHeaders createFailureAlert(String defaultMessage, String entityName) {
        log.error("Failure alert: {}", defaultMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-error", defaultMessage);
        headers.add("X-" + APPLICATION_NAME + "-params", entityName);
        return headers;
    }

    public static HttpHeaders createStatusMessageAlert(StatusMessage statusMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-flareclientApp-alert", String.format("Received '%s' status from server: %s", statusMessage.getStatusType(), statusMessage.getMessage()));
        headers.add("X-flareclientApp-params", statusMessage.getMessageId());
        return headers;
    }
}
