package com.finsight.app.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.finsight.app.dao.RawImportFileRepository;
import com.finsight.app.dao.RawTransactionRepository;
import com.finsight.app.entity.RawImportFile;
import com.finsight.app.entity.RawTransaction;

@Service
public class TransactionImportService {

	public record ParseResult(int rowsParsed,	List<String> errors) {}
	private final ObjectMapper mapper;
	private final RawImportFileRepository rawImportFileRepo;
	private final RawTransactionRepository rawTransactionRepo;

	public TransactionImportService(ObjectMapper mapper, RawTransactionRepository rawTransactionRepo, RawImportFileRepository rawImportFileRepo) {
		this.mapper = mapper;
		this.rawImportFileRepo = rawImportFileRepo;
		this.rawTransactionRepo = rawTransactionRepo;
	}

	final CSVFormat format = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.setTrim(true)
			.setIgnoreEmptyLines(true)
			.build();


	@Transactional
	public ParseResult parseCsv(Long accountId, Long importProfileId, String originalFileName, InputStream in) throws IOException {

		RawImportFile fileToImport = new RawImportFile();

		MessageDigest fileDigest = sha256Digest();
		MessageDigest rowDigest = sha256Digest();

		try (DigestInputStream dIn = new DigestInputStream(in, fileDigest);
				CSVParser parser = new  CSVParser(
						new BufferedReader(
								new InputStreamReader(dIn, StandardCharsets.UTF_8)), format)) {
			
			List<String> headerNames = parser.getHeaderNames();
			String jsonHeader = mapper.writeValueAsString(headerNames);
			
			fileToImport.setAccountId(accountId);
			fileToImport.setImportProfileId(importProfileId);
			fileToImport.setOriginalFilename(originalFileName);
			fileToImport.setHeadersJson(jsonHeader);
			fileToImport.setStatus(RawImportFile.Status.RECEIVED);
			
			RawImportFile savedFile = rawImportFileRepo.saveAndFlush(fileToImport);
			Long rawFileId = savedFile.getId();
			
			savedFile.setStatus(RawImportFile.Status.INGESTING);
			rawImportFileRepo.save(savedFile);

			int batchSize = 250;
			List<RawTransaction> buffer = new ArrayList<>(batchSize);
			int csvRowCount = 0;
			
			for (CSVRecord record : parser) {
				String rowJson = mapper.writeValueAsString(record.toMap());
				rowDigest.reset();
				rowDigest.update(rowJson.getBytes(StandardCharsets.UTF_8));
				csvRowCount++;
				
				RawTransaction rawTransaction = new RawTransaction();
				rawTransaction.setRawFileId(rawFileId);
				rawTransaction.setCsvRowNumber(csvRowCount);
				rawTransaction.setRowJSON(rowJson);
				rawTransaction.setRowHash(toHexLower(rowDigest.digest()));
				buffer.add(rawTransaction);

				if (buffer.size() >= batchSize) {
					rawTransactionRepo.saveAll(buffer);
					rawTransactionRepo.flush();
					buffer.clear();
				}
			}

			if (!buffer.isEmpty()) {
				rawTransactionRepo.saveAll(buffer);
				rawTransactionRepo.flush();
				buffer.clear();
			}
			
			String contentHashHex = toHexLower(fileDigest.digest());
			
			savedFile.setRowCount(csvRowCount);
			savedFile.setContentHash(contentHashHex);
			savedFile.setFinalizedAt(Instant.now());
			savedFile.setStatus(RawImportFile.Status.FINALIZED);
			savedFile = rawImportFileRepo.saveAndFlush(savedFile);
			return new ParseResult(csvRowCount, new ArrayList<>());
		}

	}

	private static MessageDigest sha256Digest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}

	private static String toHexLower(byte[] bytes) {
		StringBuilder stringOfBytes = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			stringOfBytes.append(Character.forDigit((b >> 4) & 0xF, 16));
			stringOfBytes.append(Character.forDigit(b & 0xF, 16));
		}
		return stringOfBytes.toString();
	}
}
