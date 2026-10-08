package com.dare.digitalbankingapi.common;

import com.dare.digitalbankingapi.account.exceptions.AccountNotFoundException;
import com.dare.digitalbankingapi.transfer.exceptions.NotEqualCurrencyException;
import com.dare.digitalbankingapi.transfer.exceptions.SufficientBalanceException;
import com.dare.digitalbankingapi.user.exceptions.EmailNotAvailableException;
import com.dare.digitalbankingapi.user.exceptions.UserNotFoundException;
import com.dare.digitalbankingapi.user.exceptions.UserProfileAlreadyExistsException;
import com.dare.digitalbankingapi.user.exceptions.UserProfileNotFoundException;
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

	//Validations
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
																			   HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	//User

	@ExceptionHandler(EmailNotAvailableException.class)
	public ResponseEntity<ErrorResponse> handleEmailNotAvailableException(EmailNotAvailableException ex, HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	//Profile

	@ExceptionHandler(UserProfileAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserProfileAlreadyExistsException(UserProfileAlreadyExistsException ex, HttpServletRequest request) {

		return errorResponseBuilder(ex, request, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(UserProfileNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserProfileNotFoundException(UserProfileNotFoundException ex, HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	//Account

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex, HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.NOT_FOUND);
	}

	//Transfer


	@ExceptionHandler(NotEqualCurrencyException.class)
	public ResponseEntity<ErrorResponse> handleNotEqualCurrencyException(NotEqualCurrencyException ex, HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(SufficientBalanceException.class)
	public ResponseEntity<ErrorResponse> handleSufficientBalanceException(SufficientBalanceException ex, HttpServletRequest request) {
		return errorResponseBuilder(ex, request, HttpStatus.BAD_REQUEST);
	}


}
