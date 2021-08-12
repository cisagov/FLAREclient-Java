package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerCredentialsUnauthorizedException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;

	public ServerCredentialsUnauthorizedException() {
        super(null, ErrorConstants.SERVER_CREDENTIALS_UNAUTHORIZED,  Status.BAD_REQUEST);
    }
}
