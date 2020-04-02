package com.mindtree.expenseservice.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
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

		InvoiceInfo invoiceInfoRes = readPDFUsingIText(invoiceInfoReq);

		if (invoiceInfoRes.getAmmount() == null || invoiceInfoRes.getDate() == null || invoiceInfoRes.getMerchant() == null
				|| invoiceInfoRes.getTxnId() == null) {
			 readPDFUsingTesseract(invoiceInfoReq, invoiceInfoRes);
		}
		return invoiceInfoRes;
	}

	private void readPDFUsingTesseract(InvoiceInfoRequest invoiceInfoReq, InvoiceInfo invoiceInfoRes) throws TesseractException {
		ExpenseDTO expense = invoiceInfoReq.getExpenseDTO();
/*
		if (expense.getFileName() == null && invoiceInfoReq.getByteStream() == null) {
			return null;
		}*/
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
			processImgeText(instance.doOCR(tempFile), invoiceInfoRes);
		} finally {
			tempFile.delete();
		}
	}
	
	/**
	 * @param invoiceInfoReq 
	 * 
	 */
	private InvoiceInfo readPDFUsingIText(InvoiceInfoRequest invoiceInfoReq) {
		InvoiceInfo invoiceInfo = new InvoiceInfo();
		byte[] decoder = null;
		if (invoiceInfoReq.getByteStream() != null) {
			decoder = invoiceInfoReq.getByteStream();
		} else {
			decoder = Base64.getDecoder().decode(invoiceInfoReq.getExpenseDTO().getDocument().split(";base64,")[1]);
		}
		try {
			PdfReader reader = new PdfReader(decoder);
			int pageILikeToCheck = reader.getNumberOfPages(); // set the page or loop them all

			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			parser.processContent(pageILikeToCheck, new RenderListener() {
				@Override
				public void renderImage(ImageRenderInfo renderInfo) {
					/*
					 * PdfImageObject image; try { image = renderInfo.getImage(); if (image == null)
					 * return; System.out.println("Found image");
					 * System.out.println(renderInfo.getStartPoint()); } catch (IOException e) {
					 * e.printStackTrace(); }
					 */}

				@Override
				public void renderText(TextRenderInfo renderInfo) {
					if (renderInfo.getText().length() > 0) {
						System.out.println(renderInfo.getText());
						processText(renderInfo.getText(), invoiceInfo);
					}
				}

				@Override
				public void endTextBlock() {
				}

				@Override
				public void beginTextBlock() {
				}

			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return invoiceInfo;
	}
	 
	 /**
	  * processText
	  * @param text
	  * @param invoiceInfo
	  */
    private void processText(String text, InvoiceInfo invoiceInfo) {
			if(text.startsWith("Date : ")) {
				SimpleDateFormat sf = new SimpleDateFormat("EEE, d MMM yyyy");
				String substring = text.substring("Date : ".length());
				try {
					Date parse = sf.parse(substring.trim());
					invoiceInfo.setDate(parse);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			
			}
			else if(text.startsWith("Total Price: ")) {
				invoiceInfo.setAmmount(text.substring("Total Price: ".length()));
			}
			else if(text.startsWith("Ref: ")) {
				invoiceInfo.setTxnId(text.substring("Ref: ".length()));
			}
			else if(text.startsWith("For ")) {
				invoiceInfo.setMerchant(text.substring("For ".length(), text.indexOf(':') - 1).trim());
			}
			
		}
	
	
	
	private void processImgeText(String imgText, InvoiceInfo invoiceInfoRes) {
		System.out.println(imgText);
		String[] split = imgText.split("\n");
		if (invoiceInfoRes.getDate() == null) {
			Optional<String> date = Arrays.stream(split).filter(str -> str.startsWith("Date : ")).findFirst();
			if (date.isPresent()) {
				SimpleDateFormat sf = new SimpleDateFormat("EEE, d MMM yyyy");
				String substring = date.get().substring("Date : ".length());
				try {
					Date parse = sf.parse(substring.trim());
					invoiceInfoRes.setDate(parse);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		if (invoiceInfoRes.getAmmount() == null) {
			Optional<String> total = Arrays.stream(split).filter(str -> str.startsWith("Total Price: ")).findFirst();
			if (total.isPresent()) {
				invoiceInfoRes.setAmmount(total.get().substring("Total Price: ".length()));
			}
		}

		if (invoiceInfoRes.getTxnId() == null) {
			Optional<String> txId = Arrays.stream(split).filter(str -> str.startsWith("Ref: ")).findFirst();
			if (txId.isPresent()) {
				invoiceInfoRes.setTxnId(txId.get().substring("Ref: ".length()));
			}
		}

		if (invoiceInfoRes.getMerchant() == null) {
			for (int i = split.length - 1; i >= 0; i--) {
				String str = split[i].trim();
				if (str.startsWith("For") && str.contains(":")) {
					invoiceInfoRes.setMerchant(str.substring(3, str.indexOf(':') - 1).trim());
					break;
				}
			}
		}
	}

}
