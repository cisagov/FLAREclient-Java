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
    protected ResponseEntity<Object> handleLoginAlreadyUsedException(LoginAlreadyUsedException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    protected ResponseEntity<Object> handleEmailAlreadyUsedException(EmailAlreadyUsedException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ServerCredentialsNotFoundException.class)
    protected ResponseEntity<Object> handleServerCredentialsNotFoundException(ServerCredentialsNotFoundException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ServerNotFoundException.class)
    protected ResponseEntity<Object> handleServerNotFoundException(ServerNotFoundException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }


    @ExceptionHandler(ServerCreationException.class)
    protected ResponseEntity<Object> handleServerCreationException(ServerCreationException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(FlareClientIllegalArgumentException.class)
    protected ResponseEntity<Object> handleUserNotFoundException(FlareClientIllegalArgumentException ex) {
        APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

	@ExceptionHandler(AccountActivationException.class)
	public ResponseEntity<Object> handleAccountActivationException() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("api-register", ErrorConstants.ERR_ACTIVATIONKEY_NOT_FOUND);
		return new ResponseEntity<>(new AccountActivationException(), httpHeaders, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(AccountUpdateException.class)
	public ResponseEntity<Object> handleAccountUpdateException() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("api-account-update", ErrorConstants.ERR_ACCOUNT_UPDATE);
		return new ResponseEntity<>(new AccountUpdateException(), httpHeaders, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<Object> handleInvalidPasswordException() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("api-account-change-passowrd", ErrorConstants.ERR_ACCOUNT_UPDATE);
		return new ResponseEntity<>(new InvalidPasswordException(), httpHeaders, HttpStatus.BAD_REQUEST);
	}
	
    private ResponseEntity<Object> buildResponseEntity(APIError apiError) {
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }
}
