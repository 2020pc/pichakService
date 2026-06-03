//package com.caspian.pichak.repository;
//
//import com.caspian.pichak.model.entity.Log;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface LogDao extends JpaRepository<Log, Integer> {
//
//    @Query("SELECT DISTINCT l FROM Log l WHERE l.rrn = :rrn")
//    Error findByRRN(@Param("rrn") Integer rrn);
//}