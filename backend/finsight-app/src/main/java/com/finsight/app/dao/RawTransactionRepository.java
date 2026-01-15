package com.finsight.app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finsight.app.entity.RawTransaction;

public interface RawTransactionRepository extends JpaRepository<RawTransaction, Long> {

}
