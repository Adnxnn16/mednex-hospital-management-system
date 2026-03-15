package com.mednex.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mednex.service.ConflictException;
import com.mednex.service.NotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<?> notFound(NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<?> conflict(ConflictException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
		var errors = ex.getBindingResult().getFieldErrors().stream().collect(java.util.stream.Collectors.toMap(
				FieldError::getField,
				FieldError::getDefaultMessage,
				(a, b) -> a));
		return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> generic(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
	}
}
