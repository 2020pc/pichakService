package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "oauth_access_token")
public class OauthAccessToken {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "token_id", nullable = false, unique = true, columnDefinition = "NVARCHAR2(400)")
    private String tokenId;
    @Lob
    @Column(name = "token", columnDefinition = "BLOB")
    private byte[] token;
    @Column(name = "authentication_id", columnDefinition = "NVARCHAR2(400)")
    private String authenticationId;
    @Column(name = "user_name", nullable = false, columnDefinition = "NVARCHAR2(400)", unique = true)
    private String userName;
    @Column(name = "client_id", columnDefinition = "NVARCHAR2(400)")
    private String clientId;
    @Lob
    @Column(name = "authentication", columnDefinition = "BLOB")
    private byte[] authentication;
    @Column(name = "refresh_token", columnDefinition = "NVARCHAR2(400)")
    private String refreshToken;

    public Integer getId() {
        return this.id;
    }

    public String getTokenId() {
        return this.tokenId;
    }

    public byte[] getToken() {
        return this.token;
    }

    public String getAuthenticationId() {
        return this.authenticationId;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getClientId() {
        return this.clientId;
    }

    public byte[] getAuthentication() {
        return this.authentication;
    }

    public String getRefreshToken() {
        return this.refreshToken;
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

    public void setAuthenticationId(final String authenticationId) {
        this.authenticationId = authenticationId;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setAuthentication(final byte[] authentication) {
        this.authentication = authentication;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
