package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class AccountUpdateException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public AccountUpdateException() {
    	super(ErrorConstants.ACCOUNT_UPDATE_ERROR, "Accout update error", Status.BAD_REQUEST);
    }
}
