package com.mindtree.expenseservice.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ExpenseDTO {

	String document;
	String description;
	@NotNull
	Integer travelId;
	String fileName;
}
