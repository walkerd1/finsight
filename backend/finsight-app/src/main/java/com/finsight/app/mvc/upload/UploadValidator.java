package com.finsight.app.mvc.upload;


import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.boot.servlet.autoconfigure.MultipartProperties;



@Component
public class UploadValidator {

	private static final String EXT = ".csv";
	private final MultipartProperties multipartProperties;

	public UploadValidator(MultipartProperties multipartProperties) {
		this.multipartProperties = multipartProperties;
	}

	public UploadRequest validateCsv(MultipartFile file) {
		if (file == null) throw new UploadValidationException("No file was provided.");
		if (file.isEmpty()) throw new UploadValidationException("File is empty. File must be non empty.");
		long maxBytes = multipartProperties.getMaxFileSize().toBytes();
		if (file.getSize() > maxBytes) throw new UploadValidationException("File is too large. File must be less than 5MB.");

		String original = file.getOriginalFilename();
		String filename = StringUtils.cleanPath(original == null ? "" : original);

		// "cleanPath" helps, but we still enforce rules explicitly
		if (filename.isBlank()) throw new UploadValidationException("Invalid filename.");
		if (filename.contains("..")) throw new UploadValidationException("Invalid filename.");
		if (!filename.toLowerCase().endsWith(EXT)) throw new UploadValidationException("Only .csv files are allowed.");

		return new UploadRequest(filename, file.getSize());
	}
}
