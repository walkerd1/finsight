package com.finsight.app.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RawTransactionTest {

	@Test
	void newRawTransaction_ingestedAtIsNullBeforePersist() {
	    var tx = new RawTransaction();
	    assertThat(tx.getIngestedAt()).isNull();
	}
	
	@Test
	void toString_doesNotThrow_withNullFields() {
		var f = new RawTransaction();
		assertThatCode(f::toString).doesNotThrowAnyException();
	}
}
