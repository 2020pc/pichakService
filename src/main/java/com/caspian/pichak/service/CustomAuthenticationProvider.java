package com.caspian.pichak.service;


import com.caspian.pichak.ResourceServer.UserResource;
import com.caspian.pichak.model.dto.CustomGrantedAuthority;
import com.caspian.pichak.model.dto.CustomUserDetails;
import com.caspian.pichak.model.entity.UserAuthority;
import com.caspian.pichak.model.entity.Users;
import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.repository.CustomUserDao;
import com.caspian.pichak.repository.ErrorDao;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pb.ouc.totp.dto.OtpDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    protected Gson gson;
    @Autowired
    protected ErrorDao errorDao;
    @Autowired
    @Qualifier("otpService")
    protected OTPService otpService;
    @Autowired
    private CustomUserDao userDao;
    @Autowired
    private UserResource userResource;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    @Qualifier("loginAttemptService")
    private LoginAttemptService loginAttemptService;

    @Transactional(readOnly = true)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String ip = this.getClientIP();
        if (this.loginAttemptService.isBlocked(ip)) {
            AAAServer.logger.info("blocked: " + ip);
            throw new AuthenticationCredentialsNotFoundException("دسترسی شما به علت بروز اشکال امنیتی محدود شده است");
        } else {
            String name = authentication.getName();
            Set<GrantedAuthority> authorities = new HashSet();
            if (((Map)authentication.getDetails()).get("endpoint") != null && ((Map)authentication.getDetails()).get("endpoint").equals("atm")) {
                Users users = this.userDao.findByUsername(name);
                String password = users.getPassword();
                CustomUserDetails customUserDetails = new CustomUserDetails();
                customUserDetails.setUserName(users.getUserName());

                for(UserAuthority authority : users.getUserAuthorities()) {
                    authorities.add(new CustomGrantedAuthority(authority.getAuthority().getName()));
                }

                customUserDetails.setGrantedAuthorities(authorities);
                return new UsernamePasswordAuthenticationToken(name + "|" + null, password, authorities);
            } else {
                String mobile = ((Map)authentication.getDetails()).get("mobile").toString();
                String otp = authentication.getCredentials().toString();
                String addedTime = ((Map)authentication.getDetails()).get("addedTime").toString();
                AAAServer.logger.info("****before check otp****");
                authorities.add(new CustomGrantedAuthority("ROLE_USER"));
                OtpDTO otpDTO = this.otpService.getOtpDTO();
                otpDTO.setAddedTime(Integer.parseInt(addedTime));
                if (this.otpService.isOtpValidate(mobile, otp)) {
                    AAAServer.logger.info("****otp true****");
                    Map map = new HashMap();
                    map.put("nationalId", name);
                    String loginResponse = this.userResource.login((new Gson()).toJson(map), this.httpServletRequest);
                    JsonObject jsonObject = (JsonObject)this.gson.fromJson(loginResponse, JsonObject.class);
                    JsonElement code = jsonObject.get("error").getAsJsonObject().get("code");
                    if (code != null && code.getAsInt() == 0) {
                        this.loginAttemptService.loginSucceeded(ip);
                        JsonElement sessionId = jsonObject.get("message");
                        if (sessionId != null && sessionId.isJsonPrimitive()) {
                            JsonElement sessionElement = (JsonElement)this.gson.fromJson(sessionId.getAsString(), JsonElement.class);
                            return new UsernamePasswordAuthenticationToken(name + "|" + sessionElement.getAsString(), otp, authorities);
                        } else {
                            throw new BadCredentialsException("عدم دریافت نشست از جانب مدیریت کانال");
                        }
                    } else {
                        throw new BadCredentialsException("عدم دریافت نشست از جانب مدیریت کانال");
                    }
                } else {
                    this.loginAttemptService.loginFailed(ip);
                    throw new BadCredentialsException("کد یکبار مصرف تایید نشد لطفا مجددا تلاش نمایید");
                }
            }
        }
    }

    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private String getClientIP() {
        try {
            String xfHeader = this.httpServletRequest.getHeader("X-Forwarded-For");
            return xfHeader == null ? this.httpServletRequest.getRemoteAddr() : xfHeader.split(",")[0];
        } catch (Exception var2) {
            return "127.0.0.1";
        }
    }

    private String getClientAgent() {
        return this.httpServletRequest.getHeader("User-Agent");
    }
}
