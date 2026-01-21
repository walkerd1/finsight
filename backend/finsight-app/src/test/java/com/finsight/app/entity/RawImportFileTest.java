package com.finsight.app.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RawImportFileTest {

	@Test
	void newRawImportFile_defaultStatusToRecieved() {
		var f = new RawImportFile();
		assertThat(f.getStatus()).isEqualTo(RawImportFile.Status.RECEIVED);
	}
	
	@Test
	void setStatus_nullBecomesRecieved() {
		var f = new RawImportFile();
		f.setStatus(RawImportFile.Status.INGESTING);
		assertThat(f.getStatus()).isEqualTo(RawImportFile.Status.INGESTING);
		f.setStatus(null);
		assertThat(f.getStatus()).isEqualTo(RawImportFile.Status.RECEIVED);
	}
	
	@Test
	void newRawImportFile_createdAtIsNullBeforePersist() {
		var f = new RawImportFile();
		assertThat(f.getCreatedAt()).isNull();
	}

	@Test
	void toString_doesNotThrow_whenFieldsNull() {
	    var f = new RawImportFile();
	    assertThatCode(f::toString).doesNotThrowAnyException();
	}
}
