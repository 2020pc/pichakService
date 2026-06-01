package com.caspian.pichak.repository;

import com.caspian.pichak.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


public interface CustomUserDao extends JpaRepository<Users, Integer> {

    @Query("SELECT DISTINCT u FROM Users u WHERE u.userName = :username")
    Users findByUsername(@Param("username") String username);
}
