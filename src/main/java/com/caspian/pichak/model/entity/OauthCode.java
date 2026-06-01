package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "oauth_code")
public class OauthCode {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "code")
    private String code;
    @Lob
    @Column(name = "authentication", columnDefinition = "BLOB")
    private byte[] authentication;

    public Integer getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public byte[] getAuthentication() {
        return this.authentication;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setAuthentication(final byte[] authentication) {
        this.authentication = authentication;
    }
}
