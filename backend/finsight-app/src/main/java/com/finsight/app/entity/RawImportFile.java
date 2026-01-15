package com.finsight.app.entity;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	
	@Column(name="content_hash", length=64)
	private String contentHash;
	
	@Column(name="row_count")
	private Integer rowCount;
	
	@Column(name="headers_json", columnDefinition="json", nullable=false)
	private String headersJson;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable=false)
	private Status status = Status.RECEIVED;
	
	@Generated(event=EventType.INSERT)
	@Column(name="created_at", nullable = false, updatable = false, insertable = false)
	private Instant createdAt;
	
	@Column(name="finalized_at")
	private Instant finalizedAt;
	
	@Column(name="failed_at")
	private Instant failedAt;
	
	@Column(name="failure_reason", columnDefinition="json")
	private String failureReason;
	
	@Column(name="finalized_content_hash", insertable=false, updatable=false)
	private String finalizedContentHash;

	public RawImportFile() {
	}
	
	public enum Status {
		RECEIVED,
		INGESTING,
		FINALIZED,
		FAILED,
		DUPLICATE
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = (status == null) ? Status.RECEIVED : status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getFinalizedAt() {
		return finalizedAt;
	}

	public void setFinalizedAt(Instant finalizedAt) {
		this.finalizedAt = finalizedAt;
	}

	public Instant getFailedAt() {
		return failedAt;
	}

	public void setFailedAt(Instant failedAt) {
		this.failedAt = failedAt;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getFinalizedContentHash() {
		return finalizedContentHash;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "RawImportFile [id=" + id + ", accountId=" + accountId + ", importProfileId=" + importProfileId
				+ ", originalFilename=" + originalFilename + ", contentHash=" + contentHash + ", rowCount=" + rowCount
				+ ", headersJson=" + headersJson + ", status=" + status + ", createdAt=" + createdAt + ", finalizedAt="
				+ finalizedAt + ", failedAt=" + failedAt + ", failureReason=" + failureReason + ", finalizedContentHash=" + finalizedContentHash + "]";
	}	
}
