package com.finsight.app.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finsight.app.entity.RawImportFile;

public interface RawImportFileRepository extends JpaRepository<RawImportFile, Long> {

}
