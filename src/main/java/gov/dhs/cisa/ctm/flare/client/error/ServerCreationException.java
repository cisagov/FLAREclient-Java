package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerCreationException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public ServerCreationException() {
        super(null, ErrorConstants.SERVER_NOT_CREATED, Status.BAD_REQUEST);
    }
}
