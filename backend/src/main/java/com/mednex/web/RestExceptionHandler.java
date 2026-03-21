package com.mednex.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.mednex.service.ConflictException;
import com.mednex.service.NotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class RestExceptionHandler {

	private static String path(WebRequest request) {
		return request.getDescription(false).replace("uri=", "");
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiErrorResponse> notFound(NotFoundException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ApiErrorResponse.of(404, "Not Found", ex.getMessage(), path(request)));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> conflict(ConflictException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ApiErrorResponse.of(409, "Conflict", ex.getMessage(), path(request)));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> dataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
		String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
		if (msg != null && (msg.contains("duplicate") || msg.contains("unique") || msg.contains("constraint"))) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiErrorResponse.of(409, "Conflict", "Duplicate booking or constraint violation", path(request)));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ApiErrorResponse.of(409, "Conflict", msg != null ? msg : "Data integrity violation", path(request)));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, WebRequest request) {
		List<ApiErrorResponse.FieldErrorEntry> errors = ex.getBindingResult().getFieldErrors().stream()
			.map(e -> new ApiErrorResponse.FieldErrorEntry(e.getField(), e.getDefaultMessage()))
			.collect(Collectors.toList());
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.validation("Validation failed", path(request), errors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> constraintViolation(ConstraintViolationException ex, WebRequest request) {
		List<ApiErrorResponse.FieldErrorEntry> errors = ex.getConstraintViolations().stream()
			.map(cv -> new ApiErrorResponse.FieldErrorEntry(
				lastPathSegment(cv.getPropertyPath()),
				cv.getMessage()))
			.collect(Collectors.toList());
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.validation("Validation failed", path(request), errors));
	}

	private static String lastPathSegment(jakarta.validation.Path path) {
		String p = path.toString();
		int idx = p.lastIndexOf('.');
		return idx >= 0 ? p.substring(idx + 1) : p;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> messageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of(400, "Bad Request", "Invalid request body", path(request)));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> accessDenied(AccessDeniedException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(ApiErrorResponse.of(403, "Forbidden", ex.getMessage(), path(request)));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> authentication(AuthenticationException ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(ApiErrorResponse.of(401, "Unauthorized", ex.getMessage(), path(request)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> generic(Exception ex, WebRequest request) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiErrorResponse.of(500, "Internal Server Error", "Internal server error", path(request)));
	}
}
