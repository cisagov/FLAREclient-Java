package gov.dhs.cisa.ctm.flare.client.error;

import org.mitre.taxii.messages.xml11.StatusMessage;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.util.HashMap;
import java.util.Map;

public class StatusMessageResponseException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;
	private final StatusMessage statusMessage;
    private final String message;

    public StatusMessageResponseException(StatusMessage statusMessage) {
        super(ErrorConstants.DEFAULT_TYPE, statusMessage.getMessage(), Status.INTERNAL_SERVER_ERROR, statusMessage.getStatusType(), null, null, getAlertParameters(statusMessage));
        this.statusMessage = statusMessage;
        this.message = String.format("%s - %s", statusMessage.getStatusType(), statusMessage.getMessage());
    }

    public StatusMessage getStatusMessage() {
        return statusMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    private static Map<String, Object> getAlertParameters(StatusMessage statusMessage) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", statusMessage.getMessage());
        parameters.put("params", "status");
        return parameters;
    }

}
