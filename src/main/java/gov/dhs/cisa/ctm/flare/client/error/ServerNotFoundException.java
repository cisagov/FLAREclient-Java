package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerNotFoundException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public ServerNotFoundException() {
        super(null, ErrorConstants.SERVER_NOT_FOUND, Status.BAD_REQUEST);
    }
}
