package com.bcmc.xor.flare.client.error;

import com.bcmc.xor.flare.client.util.HeaderUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.spring.web.advice.validation.ConstraintViolationProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";

    private String applicationName;
    
    /**
     * Post-process Problem payload to add the message key for front-end if needed
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
 
        if (entity == null || entity.getBody() == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
            .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with("path", request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                .with("violations", ((ConstraintViolationProblem) problem).getViolations())
                .with("message", ErrorConstants.ERR_VALIDATION);
            return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
        } else {
            builder
                .withCause(((DefaultProblem) problem).getCause())
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);
            if (!problem.getParameters().containsKey("message") && problem.getStatus() != null) {
                builder.with("message", "error.http." + problem.getStatus().getStatusCode());
            }
            return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
        }
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM(f.getObjectName(), f.getField(), f.getCode()))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with("message", ErrorConstants.ERR_VALIDATION)
            .with("fieldErrors", fieldErrors)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Problem> handleNoSuchElementException(NoSuchElementException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.NOT_FOUND)
            .with("message", ErrorConstants.ENTITY_NOT_FOUND_TYPE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Problem> handleNotFoundException(NotFoundException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.NOT_FOUND)
            .with("message", ErrorConstants.ENTITY_NOT_FOUND_TYPE)
            .build();
        return create(ex, problem, request);
    }


    @ExceptionHandler(BadRequestAlertException.class)
    public ResponseEntity<Problem> handleBadRequestAlertException(BadRequestAlertException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(ex.getMessage(), ex.getEntityName()));
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with("message", ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Problem> handleGenericInternalFailure(InternalServerErrorException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withType(ErrorConstants.INTERNAL_TYPE)
            .withTitle("Internal Server Error")
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .with("message", ex.getMessage())
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(StatusMessageResponseException.class)
    public ResponseEntity<Problem> handleStatusMessageResponse(StatusMessageResponseException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(String.format("Received status message from server: %s - %s", ex.getStatusMessage().getStatusType(), ex.getStatusMessage().getMessage()),
            "status"));
    }
}
