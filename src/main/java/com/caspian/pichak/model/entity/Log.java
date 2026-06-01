package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "log")
public class Log {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "USERNAME", nullable = false)
    private String username;
    @Column(name = "RRN", nullable = false, columnDefinition = "NUMBER(16)")
    private Long rrn;
    @Column(name = "URL", nullable = false, columnDefinition = "NVARCHAR2(500)")
    private String url;
    @Column(name = "CLIENT", nullable = false, columnDefinition = "NVARCHAR2(1048576)")
    private String client;
    @Column(name = "MESSAGE", columnDefinition = "NVARCHAR2(1048576)")
    private String message;
    @Column(name = "ERROR", columnDefinition = "NVARCHAR2(1048576)")
    private String error;
    @Column(name = "ENDPOINT", nullable = false)
    private String endpoint;

    public Integer getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public Long getRrn() {
        return this.rrn;
    }

    public String getUrl() {
        return this.url;
    }

    public String getClient() {
        return this.client;
    }

    public String getMessage() {
        return this.message;
    }

    public String getError() {
        return this.error;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setRrn(final Long rrn) {
        this.rrn = rrn;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setClient(final String client) {
        this.client = client;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setError(final String error) {
        this.error = error;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }
}
