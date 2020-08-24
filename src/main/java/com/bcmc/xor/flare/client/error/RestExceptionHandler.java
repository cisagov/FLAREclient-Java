package com.bcmc.xor.flare.client.error;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler  extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        APIError apiError = new APIError(HttpStatus.UNAUTHORIZED);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

	@ExceptionHandler(LoginAlreadyUsedException.class)
	public ResponseEntity<Object> handleLoginAlreadyUsedException() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("api-register", ErrorConstants.ERR_LOGIN_IN_USED);
		System.out.println("************1111111111RestExceptionHandler:LoginAlreadyUsedException1111111111*************************");
		return new ResponseEntity<>(new LoginAlreadyUsedException(), httpHeaders, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(EmailAlreadyUsedException.class)
	public ResponseEntity<Object> handleEmailAlreadyUsedException() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("api-register", ErrorConstants.ERR_EMAIL_IN_USED);
		System.out.println("************1111111111RestExceptionHandler:EmailAlreadyUsedException1111111111*************************");
		return new ResponseEntity<>(new EmailAlreadyUsedException(), httpHeaders, HttpStatus.CONFLICT);
	}
    private ResponseEntity<Object> buildResponseEntity(APIError apiError) {
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }
}
