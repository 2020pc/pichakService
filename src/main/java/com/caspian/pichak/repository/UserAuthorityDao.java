package com.caspian.pichak.repository;


import com.caspian.pichak.model.entity.UserAuthority;
import com.caspian.pichak.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAuthorityDao extends JpaRepository<UserAuthority, Integer> {
    @Query("SELECT DISTINCT u FROM UserAuthority u WHERE u.users = :users")
    UserAuthority findByUser(@Param("users") Users users);
}