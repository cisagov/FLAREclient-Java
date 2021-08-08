package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class AccountActivationException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public AccountActivationException() {
    	super(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Activation Key Error", Status.CONFLICT);
    }
}
