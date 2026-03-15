package com.mednex.service;

public class ConflictException extends RuntimeException {
	public ConflictException(String message) {
		super(message);
	}
}
