package com.mindtree.expenseservice.service;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mindtree.expenseservice.dto.ExpenseDTO;
import com.mindtree.expenseservice.dto.InvoiceInfo;
import com.mindtree.expenseservice.dto.InvoiceInfoRequest;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class ExpenseServiceImpl implements ExpenseService {
	
	@Value("${app.datapath}")
	private String datapath;
	@Override
	public InvoiceInfo getInvoiceInfo(InvoiceInfoRequest invoiceInfoReq) throws TesseractException {
		ExpenseDTO expense = invoiceInfoReq.getExpenseDTO();

		if (expense.getFileName() == null && invoiceInfoReq.getByteStream() == null) {
			return null;
		}
		String fileName = expense.getFileName();
		if (fileName == null) {
			fileName = "tmp.pdf";
		}
		byte[] decoder = invoiceInfoReq.getByteStream();
		if (decoder == null) {
			decoder = Base64.getDecoder().decode(expense.getDocument().split(";base64,")[1]);
		}
		String property = System.getProperty("java.io.tmpdir");
        File tempFile = new File(property + "/" + fileName);
		try (FileOutputStream fos = new FileOutputStream(tempFile);) {
			fos.write(decoder);

		} catch (Exception e) {
			e.printStackTrace();
		}
		ITesseract instance = new Tesseract();
		try {
			//URL resource = getClass().getResource(System.getProperty("java.io.tmpdir") );
			instance.setDatapath(datapath);
			return processImgeText(instance.doOCR(tempFile));
		} finally {
			tempFile.delete();
		}
	}
	
	private InvoiceInfo processImgeText(String imgText) {
		System.out.println(imgText);
		InvoiceInfo resp = new InvoiceInfo();
		String[] split = imgText.split("\n");
		Optional<String> date = Arrays.stream(split).filter(str -> str.startsWith("Date : ")).findFirst();
		if (date.isPresent()) {
			SimpleDateFormat sf = new SimpleDateFormat("EEE, d MMM yyyy");
			String substring = date.get().substring("Date : ".length());
			try {
				Date parse = sf.parse(substring.trim());
				resp.setDate(parse);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		Optional<String> total = Arrays.stream(split).filter(str -> str.startsWith("Total Price: ")).findFirst();
		if (total.isPresent()) {
			resp.setAmmount(total.get().substring("Total Price: ".length()));
		}

		Optional<String> txId = Arrays.stream(split).filter(str -> str.startsWith("Ref: ")).findFirst();
		if (txId.isPresent()) {
			resp.setTxnId(txId.get().substring("Ref: ".length()));
		}
		for (int i = split.length - 1; i >= 0; i--) {
			String str = split[i].trim();
			if (str.startsWith("For") && str.contains(":")) {
				resp.setMerchant(str.substring(3, str.indexOf(':') - 1).trim());
				break;
			}
		}
		return resp;
	}

}
