package com.dare.digitalbankingapi.common.validation;

import com.dare.digitalbankingapi.common.exceptions.InvalidSortFieldException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SortValidator {

	public void validate(Pageable pageable, Set<String> allowedFields) {

		pageable.getSort().forEach(order -> {

			if (!allowedFields.contains(order.getProperty())) {
				throw new InvalidSortFieldException(
						order.getProperty(),
						allowedFields
				);
			}
		});
	}
}