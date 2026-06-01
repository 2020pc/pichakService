package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "oauth_refresh_token")
public class OauthRefreshToken {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "token_id", unique = true)
    private String tokenId;
    @Lob
    @Column(name = "token", columnDefinition = "BLOB")
    private byte[] token;
    @Lob
    @Column(name = "authentication", columnDefinition = "BLOB")
    private byte[] authentication;

    public Integer getId() {
        return this.id;
    }

    public String getTokenId() {
        return this.tokenId;
    }

    public byte[] getToken() {
        return this.token;
    }

    public byte[] getAuthentication() {
        return this.authentication;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setTokenId(final String tokenId) {
        this.tokenId = tokenId;
    }

    public void setToken(final byte[] token) {
        this.token = token;
    }

    public void setAuthentication(final byte[] authentication) {
        this.authentication = authentication;
    }
}