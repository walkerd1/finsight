package com.finsight.app.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="raw_transactions")
public class RawTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@Column(name="raw_file_id", nullable=false)
	private Long rawFileId;
	
	@Column(name="row_number", nullable=false)
	private Integer rowNumber;
	
	@Column(name="row_json", columnDefinition="json", nullable=false)
	private String rowJSON;
	
	@Column(name="row_hash", length=64, nullable=false)
	private String rowHash;
	
	@Column(name="ingested_at", updatable=false, nullable=false)
	private Instant ingestedAt;

	/**
	 * 
	 */
	protected RawTransaction() {}

	public Long getRawFileId() {
		return rawFileId;
	}

	public void setRawFileId(Long rawFileId) {
		this.rawFileId = rawFileId;
	}

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getRowJSON() {
		return rowJSON;
	}

	public void setRowJSON(String rowJSON) {
		this.rowJSON = rowJSON;
	}

	public String getRowHash() {
		return rowHash;
	}

	public void setRowHash(String rowHash) {
		this.rowHash = rowHash;
	}

	public Instant getIngestedAt() {
		return ingestedAt;
	}

	public void setIngestedAt(Instant ingetstedAt) {
		this.ingestedAt = ingetstedAt;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "RawTransactions [id=" + id + ", rawFileId=" + rawFileId + ", rowNumber=" + rowNumber + ", rowJSON="
				+ rowJSON + ", rowHash=" + rowHash + ", ingetstedAt=" + ingestedAt + "]";
	}
}
