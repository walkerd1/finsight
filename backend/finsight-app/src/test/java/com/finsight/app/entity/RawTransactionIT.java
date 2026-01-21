package com.finsight.app.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.finsight.app.dao.RawImportFileRepository;
import com.finsight.app.dao.RawTransactionRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/sql/account_and_institution_seed-core.sql")
public class RawTransactionIT {

	@Autowired RawTransactionRepository repo;
	@Autowired RawImportFileRepository rawImportFileRepo;
	@Autowired EntityManager em;

	static final Integer TEST_ROW_NUMBER = 1;
	static final String TEST_ROW_JSON = "{}";
	static final String TEST_ROW_HASH =
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

	static final Long TEST_ACCOUNT_ID = 1L;
	static final String TEST_HEADERS_JSON = "[]";
	private RawImportFile savedFile;

	@Container
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
	.withDatabaseName("finsight")
	.withUsername("test")
	.withPassword("test");

	@DynamicPropertySource
	static void dbProps(DynamicPropertyRegistry r) {
		r.add("spring.datasource.url", mysql::getJdbcUrl);
		r.add("spring.datasource.username", mysql::getUsername);
		r.add("spring.datasource.password", mysql::getPassword);
		r.add("spring.flyway.url", mysql::getJdbcUrl);
		r.add("spring.flyway.user", mysql::getUsername);
		r.add("spring.flyway.password", mysql::getPassword);
	}

	@BeforeEach
	void setupRawFile() {
		var tx = new RawImportFile();
		tx.setAccountId(TEST_ACCOUNT_ID);
		tx.setHeadersJson(TEST_HEADERS_JSON);
		savedFile = rawImportFileRepo.saveAndFlush(tx);
	}

	@Test
	void save_setsId_andDbSetsIngestedAt() {
		var tx = new RawTransaction();
		tx.setCsvRowNumber(TEST_ROW_NUMBER);
		tx.setRawFileId(savedFile.getId());
		tx.setRowHash(TEST_ROW_HASH);
		tx.setRowJSON(TEST_ROW_JSON);

		var savedTransaction = repo.saveAndFlush(tx);

		em.clear();
		
		var reloadedTransaction = repo.findById(savedTransaction.getId()).orElseThrow();
		
		assertThat(reloadedTransaction.getId()).isNotNull();
		assertThat(reloadedTransaction.getIngestedAt()).isNotNull();
	}

	@Test
	void save_fails_whenRawFileIdNull() {
		var tx = new RawTransaction();
		tx.setCsvRowNumber(TEST_ROW_NUMBER);
		tx.setRowHash(TEST_ROW_HASH);
		tx.setRowJSON(TEST_ROW_JSON);
		assertThatThrownBy(() -> repo.saveAndFlush(tx)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void save_fails_whenCsvRowNumberNull() {
		var tx = new RawTransaction();
		tx.setRawFileId(savedFile.getId());
		tx.setRowHash(TEST_ROW_HASH);
		tx.setRowJSON(TEST_ROW_JSON);

		assertThatThrownBy(() -> repo.saveAndFlush(tx)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void save_fails_whenRowJsonNull() {
		var tx = new RawTransaction();
		tx.setCsvRowNumber(TEST_ROW_NUMBER);
		tx.setRawFileId(savedFile.getId());
		tx.setRowHash(TEST_ROW_HASH);

		assertThatThrownBy(() -> repo.saveAndFlush(tx)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void save_fails_whenRowHashNull() {
		var tx = new RawTransaction();
		tx.setCsvRowNumber(TEST_ROW_NUMBER);
		tx.setRawFileId(savedFile.getId());
		tx.setRowJSON(TEST_ROW_JSON);

		assertThatThrownBy(() -> repo.saveAndFlush(tx)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void unique_rowHash_isEnforced_perFileId() {
		var txA = new RawTransaction();
		txA.setCsvRowNumber(TEST_ROW_NUMBER);
		txA.setRawFileId(savedFile.getId());
		txA.setRowHash(TEST_ROW_HASH);
		txA.setRowJSON(TEST_ROW_JSON);
		repo.saveAndFlush(txA);

		var txB = new RawTransaction();
		txB.setCsvRowNumber(TEST_ROW_NUMBER);
		txB.setRawFileId(savedFile.getId());
		txB.setRowHash(TEST_ROW_HASH);
		txB.setRowJSON(TEST_ROW_JSON);

		assertThatThrownBy(() -> repo.saveAndFlush(txB))
		.isInstanceOf(DataIntegrityViolationException.class);
	}
}
