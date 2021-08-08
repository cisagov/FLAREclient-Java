package gov.dhs.cisa.ctm.flare.client.error;

import java.util.HashMap;
import java.util.Map;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import gov.dhs.cisa.ctm.taxii2.messages.TaxiiError;

public class TaxiiErrorResponseException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;
	private final TaxiiError taxiiError;
	private final String message;

	public TaxiiErrorResponseException(TaxiiError taxiiError) {
		super(ErrorConstants.DEFAULT_TYPE, taxiiError.getDescription(), Status.INTERNAL_SERVER_ERROR,
				taxiiError.getErrorCode(), null, null, getAlertParameters(taxiiError));
		this.taxiiError = taxiiError;
		this.message = String.format("%s - %s", taxiiError.getErrorCode(), taxiiError.getDescription());
	}

	@Override
	public String getMessage() {
		return message;
	}

	public TaxiiError getTaxiiError() {
		return taxiiError;
	}

	private static Map<String, Object> getAlertParameters(TaxiiError taxiiError) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("message", taxiiError.getDescription());
		parameters.put("params", "status");
		parameters.putAll(taxiiError.getCustomProperties());
		return parameters;
	}

}
