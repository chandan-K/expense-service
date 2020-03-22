/**
 * 
 */
package com.mindtree.expenseservice.dto;

import lombok.Data;

/**
 * @author M1026334
 *
 */

@Data
public class InvoiceInfoRequest {
	private byte[] byteStream;
	private ExpenseDTO expenseDTO;
}
