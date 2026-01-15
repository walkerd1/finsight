package com.finsight.app.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.finsight.app.service.TransactionImportService;
import com.finsight.app.service.TransactionImportService.ParseResult;

@Controller
public class TransactionUploadController {

	private static final Logger log = LoggerFactory.getLogger(TransactionUploadController.class);
	private final TransactionImportService importService;

	@Value("${finsight.upload-dir}")
	private String uploadDir;

	public TransactionUploadController(TransactionImportService importService) {
        this.importService = importService;
    }
	
	@GetMapping("/upload")
	public String getUploadPage() {
		return "upload";
	}


	@PostMapping("/upload")
	public String handleCSVFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

		//Return early and warn of empty file
		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "File is empty. File must be non empty.");
			return "redirect:/upload";
		} 

		//Return early and warn of file larger than 5mb
		if (file.getSize() > 5 * 1024 * 1024) {
			redirectAttributes.addFlashAttribute("message", "File is too large. File must be less than 5MB.");
			return "redirect:/upload";
		}

		String originalName = file.getOriginalFilename();
		String filename = StringUtils.cleanPath(originalName == null ? "" : originalName);

		//Return early and warn of malformed filename
		if (filename.isBlank() || filename.contains("..")) {
			redirectAttributes.addFlashAttribute("message", "Invalid filename.");
			return "redirect:/upload";
		}

		//Return early and warn of wrong file extension
		if (!filename.toLowerCase().endsWith(".csv")) {
			redirectAttributes.addFlashAttribute("message", "Only .csv files are allowed.");
			return "redirect:/upload";
		}

		Path base = uploadBasePath();
		Path destination = base.resolve(filename).normalize();

		//Return early and warn of mismatched upload file path
		if (!destination.startsWith(base)) {
			redirectAttributes.addFlashAttribute("message", "Invalid upload path.");
			return "redirect:/upload";
		}

		try {

			Files.createDirectories(base); // Ensure directory exists
			try (InputStream in = file.getInputStream()) {
				ParseResult parsedResult = importService.parseCsv(1L, 1L, filename, in);
				redirectAttributes.addFlashAttribute("message", "File successfully uploaded!");
				redirectAttributes.addFlashAttribute("uploadedName", filename);
				redirectAttributes.addFlashAttribute("uploadedSize", file.getSize());
				redirectAttributes.addFlashAttribute("rowCount", parsedResult.rowsParsed());
				return "redirect:/upload";
			}
		} catch (IOException e) {
			log.error("Upload failed for file: {}", filename, e);
			redirectAttributes.addFlashAttribute("message", "Failed to upload file.");
			return "redirect:/upload";
		}
	}

	private Path uploadBasePath() {
		return Paths.get(uploadDir).toAbsolutePath().normalize();
	}
}
