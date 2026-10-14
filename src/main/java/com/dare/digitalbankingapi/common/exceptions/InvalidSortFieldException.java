package com.dare.digitalbankingapi.common.exceptions;

import lombok.Getter;

import java.util.Set;

@Getter
public class InvalidSortFieldException extends RuntimeException {

	private final String field;
	private final Set<String> allowedFields;

	public InvalidSortFieldException(String field, Set<String> allowedFields) {
		this.field = field;
		this.allowedFields = allowedFields;
	}
}

