package com.mindtree.expenseservice.dto;

import java.util.Date;

import lombok.Data;

@Data
public class InvoiceInfo {
	private Date date;
	private String merchant;
	private String txnId;
	private String ammount;
}
