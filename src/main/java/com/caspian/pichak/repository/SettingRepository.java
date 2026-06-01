package com.caspian.pichak.repository;

import com.caspian.pichak.model.entity.Setting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, Integer> {
    @Query("SELECT DISTINCT s FROM Setting s WHERE s.isDeleted = 0")
    List<Setting> findAllActive(Pageable pageable);

    @Query("SELECT DISTINCT s FROM Setting s WHERE s.isDeleted = 0")
    List<Setting> findAllActive();

    @Query("SELECT DISTINCT s FROM Setting s WHERE s.isDeleted = 0 AND s.key = :key")
    Setting findByKey(@Param("key") String key);

    @Query("SELECT count(*) FROM Setting s WHERE s.isDeleted = 0")
    long countActive();
}
