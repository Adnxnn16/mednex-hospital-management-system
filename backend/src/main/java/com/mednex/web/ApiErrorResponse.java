package com.mednex.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * PRD-compliant API error response body.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
	String timestamp,
	int status,
	String error,
	String message,
	String path,
	List<FieldErrorEntry> fieldErrors
) {
	public static ApiErrorResponse of(int status, String error, String message, String path) {
		return new ApiErrorResponse(
			Instant.now().toString(),
			status,
			error,
			message,
			path,
			null
		);
	}

	public static ApiErrorResponse validation(String message, String path, List<FieldErrorEntry> fieldErrors) {
		return new ApiErrorResponse(
			Instant.now().toString(),
			400,
			"Bad Request",
			message,
			path,
			fieldErrors
		);
	}

	public record FieldErrorEntry(String field, String message) {}
}
