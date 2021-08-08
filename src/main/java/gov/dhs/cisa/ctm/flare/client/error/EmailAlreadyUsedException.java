package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class EmailAlreadyUsedException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException() {
        super(null,ErrorConstants.EMAIL_ALREADY_USED, Status.BAD_REQUEST);
    }
}
