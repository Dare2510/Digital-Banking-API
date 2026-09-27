package com.dare.digitalbankingapi.common;

import java.time.LocalDateTime;

public record ErrorResponse(
		int httpStatus,
		String message,
		String path,
		LocalDateTime timestamp) {

}
