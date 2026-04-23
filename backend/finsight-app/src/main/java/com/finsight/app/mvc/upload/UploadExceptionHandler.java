package com.finsight.app.mvc.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(UploadExceptionHandler.class);
  private static final String REDIRECT_UPLOAD = "redirect:/upload";

  @ExceptionHandler(UploadValidationException.class)
  public String handleValidation(UploadValidationException ex, RedirectAttributes ra) {
    log.debug("Upload validation failed: {}", ex.getMessage());
    ra.addFlashAttribute("message", ex.getMessage());
    return REDIRECT_UPLOAD;
  }

  @ExceptionHandler(UploadProcessingException.class)
  public String handleProcessing(UploadProcessingException ex, RedirectAttributes ra) {
    log.error("Upload processing failed: {}", ex.getMessage(), ex);
    ra.addFlashAttribute("message", "Failed to upload file.");
    return REDIRECT_UPLOAD;
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String handleMaxSize(MaxUploadSizeExceededException ex, RedirectAttributes ra) {
    ra.addFlashAttribute("message", "File is too large. Please upload a smaller CSV.");
    return REDIRECT_UPLOAD;
  }

  @ExceptionHandler(MultipartException.class)
  public String handleMultipart(MultipartException ex, RedirectAttributes ra) {
    if (hasCause(ex, MaxUploadSizeExceededException.class)) {
      ra.addFlashAttribute("message", "File is too large. Please upload a smaller CSV.");
      return REDIRECT_UPLOAD;
    }
    log.warn("Multipart upload failed", ex);
    ra.addFlashAttribute("message", "Upload failed. Please try again.");
    return REDIRECT_UPLOAD;
  }

  @ExceptionHandler(Exception.class)
  public String handleCatchAll(Exception ex, RedirectAttributes ra) {
    log.error("Unexpected upload error", ex);
    ra.addFlashAttribute("message", "Unexpected error while uploading.");
    return REDIRECT_UPLOAD;
  }

  private boolean hasCause(Throwable t, Class<? extends Throwable> type) {
    Throwable cur = t;
    while (cur != null) {
      if (type.isInstance(cur)) return true;
      cur = cur.getCause();
    }
    return false;
  }
}
