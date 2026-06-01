package com.caspian.pichak.service;

import com.pb.ouc.totp.dto.OtpDTO;
import com.pb.ouc.totp.dto.OtpResponseDTO;
import com.pb.ouc.totp.service.TOTP;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@PropertySource({"classpath:application.properties"})
@Service("otpService")
public class OTPService {
    @Value("${otp.dto.otpValidTime}")
    private int otpValidTime;
    @Value("${otp.dto.returnDigit}")
    private int returnDigit;
    private TOTP totp;
    private OtpDTO otpDTO;

    @PostConstruct
    private void init() {
        this.otpDTO = new OtpDTO();
        this.otpDTO.setOtpValidTime(this.otpValidTime);
        this.otpDTO.setReturnDigits(this.returnDigit);
        this.totp = new TOTP(this.otpDTO);
    }

    public OtpResponseDTO sendOtp(String mobile) throws Exception {
        OtpResponseDTO otpResponseDTO = this.totp.getOtp(mobile);
        System.out.println(otpResponseDTO.toString());
        return otpResponseDTO;
    }

    public Boolean isOtpValidate(String mobile, String otpValue) {
        Boolean isValid = this.totp.isOtpValeValidate(mobile, otpValue);
        return isValid;
    }

    public void setOtpDTO(final OtpDTO otpDTO) {
        this.otpDTO = otpDTO;
    }

    public OtpDTO getOtpDTO() {
        return this.otpDTO;
    }
}
