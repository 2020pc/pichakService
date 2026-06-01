package com.caspian.pichak.repository;


import com.caspian.pichak.model.entity.Error;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ErrorDao extends JpaRepository<Error,Integer> {

    @Query("SELECT DISTINCT e FROM Error e WHERE e.code = :code")
    Error findByCode(@Param("code") Integer code);

    @Query("SELECT DISTINCT e FROM Error e WHERE e.message LIKE CONCAT('%',:message,'%')")
    Error findByMessage(@Param("message") String message);
}
