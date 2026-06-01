package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "authority",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "AUTHORITY_UNIQUE_NAME")})
public class Authority {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 20)
    private String name;

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
