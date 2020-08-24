package com.bcmc.xor.flare.client.error;

import java.net.URI;

public final class ErrorConstants {

	public static final String ERR_REQUEST_EXCEPTION = "error.requestException";
	public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
	public static final String ERR_VALIDATION = "error.validation";
	public static final String ERR_BAD_REQUEST = "error.request";
	public static final String ERR_EMAIL_IN_USED = "error.email.in.used";
	public static final String ERR_LOGIN_IN_USED = "error.login.in.used";
	public static final String ERR_ACTIVATIONKEY_NOT_FOUND = "error.activation.key.not.found";
	private static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
	public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
	public static final URI RUNTIME_TYPE = URI.create(PROBLEM_BASE_URL + "/runtime");
	public static final URI INTERNAL_TYPE = URI.create(PROBLEM_BASE_URL + "/internal");
	public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
	public static final URI PARAMETERIZED_TYPE = URI.create(PROBLEM_BASE_URL + "/parameterized");
	public static final URI ENTITY_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/entity-not-found");
	public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
	public static final URI AUTHENTICATION_FAILURE_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-credentials");
	public static final String AUTHENTICATION_FAILURE = "{ \"error\":\"Failed to authenticate\" }";;
	public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
	public static final URI DATE_FORMAT_ERROR_TYPE = URI.create(PROBLEM_BASE_URL + "/bad-date");
	public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");
	public static final URI EMAIL_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/email-not-found");
	public static final URI USER_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/user-not-found");
	public static final URI ACTIVATION_ERROR = URI.create(PROBLEM_BASE_URL + "/activation-error");

	private ErrorConstants() {
	}
}
