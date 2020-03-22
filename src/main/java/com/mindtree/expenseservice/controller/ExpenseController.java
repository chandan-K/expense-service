/**
 * 
 */
package com.mindtree.expenseservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mindtree.expenseservice.dto.InvoiceInfo;
import com.mindtree.expenseservice.dto.InvoiceInfoRequest;
import com.mindtree.expenseservice.service.ExpenseService;

import net.sourceforge.tess4j.TesseractException;

/**
 * @author M1026334
 *
 */
@RestController
@CrossOrigin
public class ExpenseController {
	
	@Autowired
	private ExpenseService ExpenseServiceImpl;
	
	@GetMapping("/api/test")
	public String test() {
		return "OK...Tested";
		
	}
	

	@PostMapping("/api/getInvoiceInfo")
	public InvoiceInfo getIvoiceInfo(@RequestBody InvoiceInfoRequest invoiceInfoReq) throws TesseractException {
		return ExpenseServiceImpl.getInvoiceInfo(invoiceInfoReq);
	}
}
