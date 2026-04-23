package com.finsight.app.mvc;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.finsight.app.mvc.upload.UploadProcessingException;
import com.finsight.app.mvc.upload.UploadRequest;
import com.finsight.app.mvc.upload.UploadValidator;
import com.finsight.app.service.TransactionImportService;
import com.finsight.app.service.TransactionImportService.ParseResult;

@Controller
public class TransactionUploadController {

	private static final Logger log = LoggerFactory.getLogger(TransactionUploadController.class);

	  private static final String UPLOAD_VIEW = "upload";
	  private static final String REDIRECT_UPLOAD = "redirect:/upload";

	  private final TransactionImportService importService;
	  private final UploadValidator uploadValidator;

	  public TransactionUploadController(TransactionImportService importService, UploadValidator uploadValidator) {
	    this.importService = importService;
	    this.uploadValidator = uploadValidator;
	  }

	  @GetMapping("/upload")
	  public String getUploadPage() {
	    return UPLOAD_VIEW;
	  }

	  @PostMapping("/upload")
	  public String handleCSVFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {

	    UploadRequest req = uploadValidator.validateCsv(file);

	    try (InputStream in = file.getInputStream()) {
	      ParseResult parsedResult = importService.parseCsv(1L, 1L, req.filename(), in);

	      ra.addFlashAttribute("message", "File successfully uploaded!");
	      ra.addFlashAttribute("uploadedName", req.filename());
	      ra.addFlashAttribute("uploadedSize", req.sizeBytes());
	      ra.addFlashAttribute("rowCount", parsedResult.rowsParsed());
	      return REDIRECT_UPLOAD;

	    } catch (Exception e) {
	      // wrap in a typed exception so ControllerAdvice handles it
	      log.warn("Upload failed for file: {}", req.filename());
	      throw new UploadProcessingException("Upload failed for file: " + req.filename(), e);
	    }
	  }
}
