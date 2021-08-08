package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class LoginAlreadyUsedException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public LoginAlreadyUsedException() {
        super(null, ErrorConstants.LOGIN_ALREADY_USED, Status.BAD_REQUEST);
    }
}
