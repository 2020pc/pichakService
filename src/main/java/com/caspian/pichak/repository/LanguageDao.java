package com.caspian.pichak.repository;

import com.caspian.pichak.model.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LanguageDao extends JpaRepository<Language, Integer> {

    @Query("SELECT DISTINCT l FROM Language l WHERE l.isoCode = :isoCode")
    Language findByIsoCode(@Param("isoCode") String isoCode);
}
