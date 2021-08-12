package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

@SuppressWarnings("unused")
public class AuthenticationFailureException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;

	public AuthenticationFailureException() {
        super(null, ErrorConstants.AUTHENTICATION_FAILURE,  Status.UNAUTHORIZED);
    }
}
