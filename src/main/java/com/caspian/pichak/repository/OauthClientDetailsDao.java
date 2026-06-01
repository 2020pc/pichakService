package com.caspian.pichak.repository;

import com.caspian.pichak.model.entity.OauthClientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthClientDetailsDao extends JpaRepository<OauthClientDetails, Integer> {

    Optional<OauthClientDetails> findByClientId(String clientId);
}