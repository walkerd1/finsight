package com.finsight.app.entity;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.finsight.app.dao.RawImportFileRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Sql("/sql/account_and_institution_seed-core.sql")
public class RawImportFileIT {

  @Autowired RawImportFileRepository repo;
  @Autowired EntityManager em;
  
  static final Long TEST_ACCOUNT_ID = 1L;
  static final String TEST_HEADERS_JSON = "[]";
  static final String TEST_CONTENT_HASH =
		  "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

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

  @Test
  void save_setsId_and_dbSetsCreatedAt() {
    var f = new RawImportFile();
    f.setAccountId(TEST_ACCOUNT_ID);
    f.setHeadersJson(TEST_HEADERS_JSON);

    var saved = repo.saveAndFlush(f);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
  }
  
  @Test
  void save_fails_whenAccountIdNull() {
	  var f = new RawImportFile();
	  f.setHeadersJson(TEST_HEADERS_JSON);
	  assertThatThrownBy(() -> repo.saveAndFlush(f)).isInstanceOf(DataIntegrityViolationException.class);
  }
  
  @Test
  void save_fails_whenHeadersJsonNull() {
	  var f = new RawImportFile();
	  f.setAccountId(TEST_ACCOUNT_ID);
	  assertThatThrownBy(() -> repo.saveAndFlush(f)).isInstanceOf(DataIntegrityViolationException.class);
  }
  
  @Test
  void finalizedContentHash_isGeneratedByDb_onlyWhenFinalized() {
    var f = new RawImportFile();
    f.setAccountId(TEST_ACCOUNT_ID);
    f.setHeadersJson(TEST_HEADERS_JSON);
    f.setContentHash(TEST_CONTENT_HASH);
    f.setStatus(RawImportFile.Status.FINALIZED);
    
    var saved = repo.saveAndFlush(f);
    
    //Clear the EntityManager to prevent cached Hibernate results failing the test.
    em.clear();
    
    var reloaded = repo.findById(saved.getId()).orElseThrow();

    assertThat(reloaded.getFinalizedContentHash()).isNotNull();
  }
  
  @Test
  void unique_finalizedHash_isEnforced_perAccount() {
    var a = new RawImportFile();
    a.setAccountId(TEST_ACCOUNT_ID);
    a.setHeadersJson(TEST_HEADERS_JSON);
    a.setContentHash(TEST_CONTENT_HASH);
    a.setStatus(RawImportFile.Status.FINALIZED);
    repo.saveAndFlush(a);

    var b = new RawImportFile();
    b.setAccountId(TEST_ACCOUNT_ID);
    b.setHeadersJson(TEST_HEADERS_JSON);
    b.setContentHash(TEST_CONTENT_HASH);
    b.setStatus(RawImportFile.Status.FINALIZED);

    assertThatThrownBy(() -> repo.saveAndFlush(b))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

}