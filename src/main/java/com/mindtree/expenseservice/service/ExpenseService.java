/**
 * 
 */
package com.mindtree.expenseservice.service;

import com.mindtree.expenseservice.dto.InvoiceInfo;
import com.mindtree.expenseservice.dto.InvoiceInfoRequest;

import net.sourceforge.tess4j.TesseractException;

/**
 * @author M1026334
 *
 */
public interface ExpenseService {
	
	InvoiceInfo getInvoiceInfo(InvoiceInfoRequest invoiceInfoReq) throws TesseractException;

}
