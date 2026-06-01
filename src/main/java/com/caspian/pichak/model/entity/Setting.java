package com.caspian.pichak.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "setting", uniqueConstraints = {@UniqueConstraint(columnNames = {"key"}, name = "SETTING_UNIQUE_KEY")})
public class Setting {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "key", nullable = false)
    private String key;
    @Column(name = "value", nullable = false)
    private String value;
    @Column(name = "isDeleted", nullable = false)
    private byte isDeleted = 0;

    public Integer getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public byte getIsDeleted() {
        return this.isDeleted;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setIsDeleted(final byte isDeleted) {
        this.isDeleted = isDeleted;
    }
}
