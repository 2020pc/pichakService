package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LANGUAGE",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"iso_code"}, name = "LANGUAGE_UNIQUE_ISO_CODE")})
public class Language {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "ISO_CODE", unique = true, nullable = false, columnDefinition = "CHAR(2)")
    private String isoCode;

    public Integer getId() {
        return this.id;
    }

    public String getIsoCode() {
        return this.isoCode;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setIsoCode(final String isoCode) {
        this.isoCode = isoCode;
    }
}
