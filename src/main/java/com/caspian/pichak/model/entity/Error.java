package com.caspian.pichak.model.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "error",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"code"}, name = "ERROR_UNIQUE_CODE")})
public class Error {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "MESSAGE", unique = true, nullable = false)
    private String message;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "CODE", nullable = false)
    private Integer code;

    public Integer getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setCode(final Integer code) {
        this.code = code;
    }
}
