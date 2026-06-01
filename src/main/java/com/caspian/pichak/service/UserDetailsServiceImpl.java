package com.caspian.pichak.service;


import com.caspian.pichak.model.dto.CustomGrantedAuthority;
import com.caspian.pichak.model.dto.CustomUserDetails;
import com.caspian.pichak.model.entity.UserAuthority;
import com.caspian.pichak.model.entity.Users;
import com.caspian.pichak.repository.CustomUserDao;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service("userDetailsService")
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private CustomUserDao userDao;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    @Qualifier("loginAttemptService")
    private LoginAttemptService loginAttemptService;

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String ip = this.getClientIP();
        if (this.loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        } else {
            if (username.contains("|")) {
                username = username.split("\\|")[0];
            }

            Users users = this.userDao.findByUsername(username);
            if (users == null) {
                throw new UsernameNotFoundException(username);
            } else {
                CustomUserDetails customUserDetails = new CustomUserDetails();
                customUserDetails.setUserName(users.getUserName());
                Set<GrantedAuthority> authorities = new HashSet();

                for(UserAuthority authority : users.getUserAuthorities()) {
                    authorities.add(new CustomGrantedAuthority(authority.getAuthority().getName()));
                }

                customUserDetails.setGrantedAuthorities(authorities);
                return customUserDetails;
            }
        }
    }

    private String getClientIP() {
        String xfHeader = this.request.getHeader("X-Forwarded-For");
        return xfHeader == null ? this.request.getRemoteAddr() : xfHeader.split(",")[0];
    }
}
