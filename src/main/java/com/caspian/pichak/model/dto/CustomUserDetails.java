package com.caspian.pichak.model.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class CustomUserDetails implements UserDetails {
    private String userName;
    private String password;
    private Set<GrantedAuthority> grantedAuthorities;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.grantedAuthorities;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.userName;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GrantedAuthority> getGrantedAuthorities() {
        return this.grantedAuthorities;
    }

    public void setGrantedAuthorities(Set<GrantedAuthority> grantedAuthorities) {
        this.grantedAuthorities = grantedAuthorities;
    }

    public String toString() {
        return "CustomUserDetails{userName='" + this.userName + '\'' + ", password='" + this.password + '\'' + ", grantedAuthorities=" + this.grantedAuthorities + '}';
    }

}
