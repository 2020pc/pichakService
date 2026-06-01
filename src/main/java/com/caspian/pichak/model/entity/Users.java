package com.caspian.pichak.model.entity;


import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_name"}, name = "USER_UNIQUE_USERNAME")})
public class Users {
    @Id
    @Column(name = "ID", unique = true, nullable = false, columnDefinition = "NUMBER(10)")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_name", unique = true, nullable = false, columnDefinition = "NVARCHAR2(50)")
    private String userName;
    @Column(name = "password", columnDefinition = "NVARCHAR2(400)")
    private String password;
    @Column(name = "account_expired", columnDefinition = "NUMBER(1)")
    private Boolean accountExpired;
    @Column(name = "account_locked", columnDefinition = "NUMBER(1)")
    private Boolean accountLocked;
    @Column(name = "credentials_expired", columnDefinition = "NUMBER(1)")
    private Boolean credentialsExpired;
    @Column(name = "enabled", columnDefinition = "NUMBER(1)")
    private Boolean enabled;
    @OneToMany(mappedBy = "users", targetEntity = UserAuthority.class,
            cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserAuthority> userAuthorities = new HashSet();

    public Integer getId() {
        return this.id;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public Boolean getAccountExpired() {
        return this.accountExpired;
    }

    public Boolean getAccountLocked() {
        return this.accountLocked;
    }

    public Boolean getCredentialsExpired() {
        return this.credentialsExpired;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public Set<UserAuthority> getUserAuthorities() {
        return this.userAuthorities;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setAccountExpired(final Boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public void setAccountLocked(final Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public void setCredentialsExpired(final Boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public void setUserAuthorities(final Set<UserAuthority> userAuthorities) {
        this.userAuthorities = userAuthorities;
    }
}

