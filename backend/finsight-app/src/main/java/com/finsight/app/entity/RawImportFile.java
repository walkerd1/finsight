package com.finsight.app.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="raw_import_files")
public class RawImportFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@Column(name="account_id", nullable=false)
	private Long accountId;
	
	@Column(name="import_profile_id")
	private Long importProfileId;
	
	@Column(name="original_filename", length=255)
	private String originalFilename;
	
	@Column(name="content_hash", length=64, nullable=false)
	private String contentHash;
	
	@Column(name="row_count")
	private Integer rowCount;
	
	@Column(name="headers_json", columnDefinition="json", nullable=false)
	private String headersJson;
	
	@Column(name="imported_at", updatable=false, nullable=false)
	private Instant importedAt;

	protected RawImportFile() {
	}

	public Long getId() {
		return id;
	}

	public Long getImportProfileId() {
		return importProfileId;
	}

	public void setImportProfileId(Long importProfileId) {
		this.importProfileId = importProfileId;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}

	public Integer getRowCount() {
		return rowCount;
	}

	public void setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public String getHeadersJson() {
		return headersJson;
	}

	public void setHeadersJson(String headersJson) {
		this.headersJson = headersJson;
	}

	public Instant getImportedAt() {
		return importedAt;
	}

	public void setImportedAt(Instant importedAt) {
		this.importedAt = importedAt;
	}

	public Long getAccountId() {
		return accountId;
	}
	
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	@Override
	public String toString() {
		return "RawImportFile [id=" + id + ", accountId=" + accountId + ", importProfileId=" + importProfileId
				+ ", originalFilename=" + originalFilename + ", contentHash=" + contentHash + ", rowCount=" + rowCount
				+ ", headersJson=" + headersJson + ", importedAt=" + importedAt + "]";
	}
	
	
}
