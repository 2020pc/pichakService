package com.caspian.pichak.repository;


import com.caspian.pichak.model.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorityDao extends JpaRepository<Authority, Integer> {

    @Query("SELECT DISTINCT u FROM Authority u WHERE u.name = :name")
    Authority findByName(@Param("name") String name);
}
