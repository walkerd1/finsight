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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.ObjectMapper;

import com.finsight.app.dao.RawImportFileRepository;
import com.finsight.app.dao.RawTransactionRepository;
import com.finsight.app.entity.RawImportFile;
import com.finsight.app.entity.RawTransaction;

@Service
public class TransactionImportService {

	public record RowFailure(int csvRowNumber, String reason) {}

	public record ImportSummary(
			long rawFileId,
			String originalFilename,
			int rowsSeen,
			int rowsInserted,
			int rowsFailed,
			String fileContentHash,
			Instant startedAt,
			Instant finishedAt,
			List<RowFailure> sampleFailures
			) {}

	private static final int DEFAULT_BATCH_SIZE = 250;
	private static final int MAX_FAILURE_SAMPLES = 50;

	private final ObjectMapper mapper;
	private final RawImportFileRepository rawImportFileRepo;
	private final RawTransactionRepository rawTransactionRepo;
	private final TransactionTemplate tx;

	private final CSVFormat format = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.setTrim(true)
			.setIgnoreEmptyLines(true)
			.build();

	public TransactionImportService(
			ObjectMapper mapper,
			RawTransactionRepository rawTransactionRepo,
			RawImportFileRepository rawImportFileRepo,
			PlatformTransactionManager transactionManager
			) {
		this.mapper = mapper;
		this.rawImportFileRepo = rawImportFileRepo;
		this.rawTransactionRepo = rawTransactionRepo;
		this.tx = new TransactionTemplate(transactionManager);
	}

	public ImportSummary parseCsv(Long accountId, Long importProfileId, String originalFileName, InputStream in) throws IOException {

		Instant startedAt = Instant.now();

		MessageDigest fileDigest = sha256Digest();
		MessageDigest rowDigest = sha256Digest();

		RawImportFile savedFile = tx.execute(status -> {
			RawImportFile f = new RawImportFile();
			f.setAccountId(accountId);
			f.setImportProfileId(importProfileId);
			f.setOriginalFilename(originalFileName);
			f.setStatus(RawImportFile.Status.RECEIVED);
			f.setHeadersJson("[]");
			return rawImportFileRepo.saveAndFlush(f);
		});

		long rawFileId = savedFile.getId();

		tx.executeWithoutResult(status -> {
			RawImportFile f = rawImportFileRepo.findById(rawFileId).orElseThrow();
			f.setStatus(RawImportFile.Status.INGESTING);
			rawImportFileRepo.save(f);
		});

		int rowsSeen = 0;
		int rowsInserted = 0;
		List<RowFailure> sampleFailures = new ArrayList<>();

		try (DigestInputStream dIn = new DigestInputStream(in, fileDigest);
				CSVParser parser = new  CSVParser(
						new BufferedReader(
								new InputStreamReader(dIn, StandardCharsets.UTF_8)), format)) {

			List<String> headerNames = parser.getHeaderNames();
			String jsonHeader = mapper.writeValueAsString(headerNames);

			tx.executeWithoutResult(status -> {
				RawImportFile f = rawImportFileRepo.findById(rawFileId).orElseThrow();
				f.setHeadersJson(jsonHeader);
				rawImportFileRepo.save(f);
			});

			List<RawTransaction> buffer = new ArrayList<>(DEFAULT_BATCH_SIZE);

			for (CSVRecord record : parser) {
				rowsSeen++;
				int csvRowNumber = rowsSeen;

				try {
					String rowJson = mapper.writeValueAsString(record.toMap());
					String canonicalRow = canonicalRowString(record, headerNames);

					rowDigest.reset();
					rowDigest.update(canonicalRow.getBytes(StandardCharsets.UTF_8));
					RawTransaction rt = new RawTransaction();
					rt.setRawFileId(rawFileId);
					rt.setCsvRowNumber(csvRowNumber);
					rt.setRowJSON(rowJson);
					rt.setRowHash(toHexLower(rowDigest.digest()));

					buffer.add(rt);

				} catch (Exception rowEx) {
					if (sampleFailures.size() < MAX_FAILURE_SAMPLES) {
						sampleFailures.add(new RowFailure(csvRowNumber, safeMessage(rowEx)));
					}
				}

				if (buffer.size() >= DEFAULT_BATCH_SIZE) {
					rowsInserted += persistBatchWithIsolation(buffer, sampleFailures);
					buffer.clear();
				}
			}

			if (!buffer.isEmpty()) {
				rowsInserted += persistBatchWithIsolation(buffer, sampleFailures);
				buffer.clear();				
			}

			String contentHashHex = toHexLower(fileDigest.digest());
			Instant finishedAt = Instant.now();
			int finalRowsSeen = rowsSeen;
			int finalRowsInserted = rowsInserted;
			int finalRowsFailed = finalRowsSeen - finalRowsInserted;
			RawImportFile.Status finalStatus =
				    finalRowsFailed > 0 ? RawImportFile.Status.FINALIZED_WITH_ERRORS : RawImportFile.Status.FINALIZED;

			tx.executeWithoutResult(status -> {
				RawImportFile f = rawImportFileRepo.findById(rawFileId).orElseThrow();
				f.setRowCount(finalRowsSeen);
				f.setContentHash(contentHashHex);
				f.setFinalizedAt(finishedAt);
				f.setStatus(finalStatus);
				rawImportFileRepo.saveAndFlush(f);

			});
			return new ImportSummary(
					rawFileId,
					originalFileName,
					finalRowsSeen,
					finalRowsInserted,
					finalRowsFailed,
					contentHashHex,
					startedAt,
					finishedAt,
					sampleFailures);
		} catch (Exception ex) {
			tx.executeWithoutResult(status -> {
				RawImportFile f = rawImportFileRepo.findById(rawFileId).orElseThrow();
				f.setStatus(RawImportFile.Status.FAILED);
				f.setFailedAt(Instant.now());
				rawImportFileRepo.save(f);
			});
			throw ex;
		}

	}

	private static String canonicalRowString(CSVRecord record, List<String> headerNames) {
		StringBuilder sb = new StringBuilder();

		for (String header : headerNames) {
			String value = record.isMapped(header) ? record.get(header) : "";

			sb.append(header)
			  .append('=')
			  .append(value == null ? "" : value.trim())
			  .append('\n');
		}

		return sb.toString();
	}
	
	private int persistBatchWithIsolation(List<RawTransaction> batch, List<RowFailure> failures) {
		try {
			return tx.execute(status -> {
				rawTransactionRepo.saveAll(batch);
				rawTransactionRepo.flush();
				return batch.size();
			});
		} catch (Exception batchEx) {
			// Batch failed. Isolate the bad row(s) by inserting one-by-one.
			int inserted = 0;
			for (RawTransaction rt : batch) {
				try {
					tx.executeWithoutResult(status -> {
						rawTransactionRepo.save(rt);
						rawTransactionRepo.flush();
					});
					inserted++;
				} catch (Exception rowDbEx) {
					if (failures.size() < MAX_FAILURE_SAMPLES) {
						failures.add(new RowFailure(rt.getCsvRowNumber(), "DB insert failed: " + safeMessage(rowDbEx)));
					}
					// skip
				}
			}
			return inserted;
		}
	}

	private static String safeMessage(Exception ex) {
		String msg = ex.getMessage();
		if (msg == null || msg.isBlank()) return ex.getClass().getSimpleName();
		// keep it short-ish for flash/UI; store full reason JSON in DB if needed
		return msg.length() > 240 ? msg.substring(0, 240) : msg;
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
