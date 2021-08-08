package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerCredentialsNotFoundException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;

	public ServerCredentialsNotFoundException() {
        super(null, ErrorConstants.CREDENTIALS_NOT_FOUND,  Status.BAD_REQUEST);
    }
}
