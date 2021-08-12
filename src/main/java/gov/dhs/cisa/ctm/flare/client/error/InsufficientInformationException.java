package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * Simple exception with a message, that returns an Internal Server Error code.
 */
public class InsufficientInformationException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;

	public InsufficientInformationException(String message) {
        super(ErrorConstants.DEFAULT_TYPE, message, Status.INTERNAL_SERVER_ERROR);
    }
}
