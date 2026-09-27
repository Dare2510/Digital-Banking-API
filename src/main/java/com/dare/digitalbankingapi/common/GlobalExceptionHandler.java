package com.dare.digitalbankingapi.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

	//HelperMethod
	private ResponseEntity<ErrorResponse> errorResponseBuilder(Exception ex, HttpServletRequest request, HttpStatus status) {
		ErrorResponse errorResponse = new ErrorResponse(
				status.value(),
				ex.getMessage(),
				request.getRequestURI(),
				LocalDateTime.now()
		);
		return new ResponseEntity<>(errorResponse, status);
	}

	//Handle Exceptions

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex ,
																			   HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}
}
