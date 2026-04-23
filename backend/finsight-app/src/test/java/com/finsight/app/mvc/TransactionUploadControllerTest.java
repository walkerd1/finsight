package com.finsight.app.mvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finsight.app.service.TransactionImportService;
import com.finsight.app.service.TransactionImportService.ImportSummary;

@WebMvcTest(TransactionUploadController.class)
class TransactionUploadControllerTest {

	static final String TEST_CONTENT_HASH =
			  "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionImportService importService;

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("finsight.upload-dir", () -> tempDir.toString());
    }

    @Test
    void getUploadPage_returnsUploadView() throws Exception {
        mockMvc.perform(get("/upload"))
               .andExpect(status().isOk())
               .andExpect(view().name("upload"));
    }
    
    @Test
    void shouldRedirectWithSuccessMessage_whenValidCsvUploaded() throws Exception {
    	Instant startedAt = Instant.now();
    	ClassPathResource resource =
    		    new ClassPathResource("testdata/mockTransactions.csv");
    	byte[] csvBytes = resource.getInputStream().readAllBytes();
    	MockMultipartFile file = new MockMultipartFile(
    			"file",
    			"transactions.csv",
    			"text/csv",
    			csvBytes);
    	
    	when(importService.parseCsv(anyLong(), anyLong(), anyString(), any(InputStream.class))).thenReturn(
    			new ImportSummary(
    					1,
    					"transactions.csv",
    					5,
    					5,
    					0,
    					TEST_CONTENT_HASH,
    					startedAt,
    					Instant.now(),
    					new ArrayList<>()
    					));
    	
    	mockMvc.perform(multipart("/upload").file(file))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/upload"))
        .andExpect(flash().attribute("message", "File successfully uploaded!"))
        .andExpect(flash().attribute("uploadedName", "transactions.csv"))
        .andExpect(flash().attribute("uploadedSize", file.getSize()))
        .andExpect(flash().attribute("rowCount", 5));
    	
    	verify(importService, times(1))
        .parseCsv(eq(1L), eq(1L), eq("transactions.csv"), any(InputStream.class));
    }
    
    @Test
    void shouldRedirectWithErrorMessage_whenEmptyCsvUploaded() throws Exception {
    	ClassPathResource resource =
    		    new ClassPathResource("testdata/emptyTransactions.csv");
    	byte[] csvBytes = resource.getInputStream().readAllBytes();
    	MockMultipartFile file = new MockMultipartFile(
    			"file",
    			"transactions.csv",
    			"text/csv",
    			csvBytes);
    	
    	
    	mockMvc.perform(multipart("/upload").file(file))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/upload"))
        .andExpect(flash().attribute("message", "File is empty. File must be non empty."));
    	
    	verify(importService, never())
        .parseCsv(anyLong(), anyLong(), anyString(), any(InputStream.class));    	
    }
}
