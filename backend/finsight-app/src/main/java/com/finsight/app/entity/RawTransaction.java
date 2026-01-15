package com.finsight.app.entity;

import java.time.Instant;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

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
	
	@Column(name="csv_row_number", nullable=false)
	private Integer csvRowNumber;
	
	@Column(name="row_json", columnDefinition="json", nullable=false)
	private String rowJSON;
	
	@Column(name="row_hash", length=64, nullable=false)
	private String rowHash;
	
	@Generated(event=EventType.INSERT)
	@Column(name="ingested_at", nullable = false, updatable = false, insertable = false)
	private Instant ingestedAt;

	/**
	 * 
	 */
	public RawTransaction() {}

	public Long getRawFileId() {
		return rawFileId;
	}

	public void setRawFileId(Long rawFileId) {
		this.rawFileId = rawFileId;
	}

	public Integer getCsvRowNumber() {
		return csvRowNumber;
	}

	public void setCsvRowNumber(Integer csvRowNumber) {
		this.csvRowNumber = csvRowNumber;
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

	public void setIngestedAt(Instant ingestedAt) {
		this.ingestedAt = ingestedAt;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "RawTransactions [id=" + id + ", rawFileId=" + rawFileId + ", rowNumber=" + csvRowNumber + ", rowJSON="
				+ rowJSON + ", rowHash=" + rowHash + ", ingestedAt=" + ingestedAt + "]";
	}
}
