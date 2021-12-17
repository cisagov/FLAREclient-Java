package com.bcmc.xor.flare.client.error;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Objects;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	// Default Handler for any all errors that don't have specified handlers
	@ExceptionHandler({Exception.class})
	protected ResponseEntity<Object> handleAll(Exception ex) {
		APIError apiError = new APIError(HttpStatus.INTERNAL_SERVER_ERROR);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ServerCredentialsUnauthorizedException.class)
	protected ResponseEntity<Object> handleServerCredentialsUnauthorizedException(ServerCredentialsUnauthorizedException ex) {
		APIError apiError = new APIError(HttpStatus.UNAUTHORIZED);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ManifestNotSupportedException.class)
	protected ResponseEntity<Object> handleManifestNotSupportedException(ManifestNotSupportedException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ServerDiscoveryException.class)
	protected ResponseEntity<Object> handleServerCredentialsUnauthorizedException(ServerDiscoveryException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

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

	@ExceptionHandler(FlareServiceUnavailableException.class)
	protected ResponseEntity<Object> handleUserNotFoundException(FlareServiceUnavailableException ex) {
		APIError apiError = new APIError(HttpStatus.SERVICE_UNAVAILABLE);
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

	@ExceptionHandler(CollectionNotFoundException.class)
	protected ResponseEntity<Object> handleCollectionNotFoundException(CollectionNotFoundException ex) {
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

	@ExceptionHandler(ServerLabelAlreadyExistsException.class)
	protected ResponseEntity<Object> handleServerLabelAlreadyExistsException(ServerLabelAlreadyExistsException ex) {
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
	public ResponseEntity<Object> handleAccountActivationException(AccountActivationException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(AccountUpdateException.class)
	public ResponseEntity<Object> handleAccountUpdateException(AccountUpdateException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<Object> handleInvalidPasswordException(InvalidPasswordException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(AuthenticationFailureException.class)
	protected ResponseEntity<Object> handleAuthenticationFailureException(AuthenticationFailureException ex) {
		APIError apiError = new APIError(HttpStatus.UNAUTHORIZED);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}


	@Override
	protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(Objects.requireNonNull(ex.getMessage()));
		return buildResponseEntity(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(Objects.requireNonNull(ex.getMessage()));
		return buildResponseEntity(apiError);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	private ResponseEntity<Object> buildResponseEntity(APIError apiError) {
		return new ResponseEntity<>(apiError, apiError.getHttpStatus());
	}
}
