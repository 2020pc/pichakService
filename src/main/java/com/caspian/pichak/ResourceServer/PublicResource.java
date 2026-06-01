package com.caspian.pichak.ResourceServer;


import com.caspian.banking.accounting.dealPosting.dto.VirtualLoanCommissionDTO;
import com.caspian.banking.accounting.dealPosting.message.GetVirtualLoanCommissionMsg;
import com.caspian.banking.cif.message.MilitaryStatusInquiryReportMsg;
import com.caspian.banking.deposit.samacheque.dto.SamaInputPersonInfoDto;
import com.caspian.banking.deposit.samacheque.message.GetBouncedChequesByPersonInfoMsg;
import com.caspian.banking.ebanking.dto.BeOpenedDepositDto;
import com.caspian.banking.ebanking.dto.CustomerAddressDto;
import com.caspian.banking.ebanking.dto.DepositSearchDto;
import com.caspian.banking.ebanking.dto.OpenDepositDto;
import com.caspian.banking.ebanking.message.MGGetBeOpenedDepositsMsg;
import com.caspian.banking.ebanking.message.MGGetCustomerAddressMsg;
import com.caspian.banking.ebanking.message.MGGetDepositsMsg;
import com.caspian.banking.ebanking.message.MGOpenDepositMsg;
import com.caspian.banking.ebanking.message.card.MGGetAllCustomerMobileNumbersMsg;
import com.caspian.banking.ebanking.message.card.MGLoadCardCustomerInfoByNumberMsg;
import com.caspian.banking.lending.message.samat.applicant.ApplicantDebitInquiryMsg;
import com.caspian.banking.lending.message.samat.applicant.ApplicantGuarantorInquiryMsg;
import com.caspian.banking.lending.message.samat.applicant.ApplicantObligationInquiryMsg;
import com.caspian.banking.lending.message.spl.GetCustomerInformationMsg;
import com.caspian.banking.model.dto.ChCustomerExistenceInquiryRequestDto;
import com.caspian.banking.model.messages.MGCustomerExistenceInquiryMsg;
import com.caspian.banking.outputmanagement.message.SendSmsForSelectedMobileNumberMsg;
import com.caspian.moderngateway.core.channelmanagerinfrastructure.exception.ChannelManagerException;
import com.caspian.moderngateway.core.channelmanagerinfrastructure.util.ChannelType;
import com.caspian.moderngateway.core.coreservice.dto.card.ChCardCustomerInfoResponseBean;
import com.caspian.moderngateway.core.coreservice.dto.card.ChContactInfoBean;
import com.caspian.moderngateway.core.coreservice.dto.card.ChLoadCardCustomerInfoByNumberRequestBean;
import com.caspian.moderngateway.core.domainmodel.dto.user.otheruser.ChOtherUserGender;
import com.caspian.moderngateway.core.domainmodel.dto.user.otheruser.ChOtherUserInfoRequestBean;
import com.caspian.moderngateway.core.domainmodel.dto.user.otheruser.VirtualOtherUserRequestBean;
import com.caspian.moderngateway.core.message.card.LoadCardCustomerInfoByNumberMsg;
import com.caspian.moderngateway.core.message.otheruser.CreateModernGatewayOtherUserMsg;
import com.caspian.pichak.exceptions.CoreException;
import com.caspian.pichak.model.dto.PichakError;
import com.caspian.pichak.model.dto.ResponseMessage;
import com.caspian.pichak.model.entity.Authority;
import com.caspian.pichak.model.entity.UserAuthority;
import com.caspian.pichak.model.entity.Users;
import com.caspian.pichak.service.lotus.LotusJmsService;
import com.caspian.pichak.service.lotus.RequestType;
import com.caspian.pichak.type.NationalCodeType;
import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.utility.Util;
import com.caspian.pichak.repository.SettingRepository;
import com.caspian.pichak.repository.UserAuthorityDao;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pb.ouc.totp.dto.OtpResponseDTO;
import com.pb.ouc.util.util.HttpParamMaker;
import com.pb.ouc.util.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping({"/OTP/public"})
public class PublicResource extends BaseResource {
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private UserAuthorityDao userAuthorityDao;
    @Autowired
    private LotusJmsService lotusJmsService;
    @Value("${lotus.core.username}")
    private String coreUsername;
    @Value("${lotus.core.branchcode}")
    private String coreBranchcode;
    @Value("${otp.pre.text}")
    private String otpPreText;
    @Value("${otp.post.text}")
    private String otpPostText;
    @Value("${otp.post.vekalati}")
    private String otpPostVekalati;
    @Value("${otp.post.pichak}")
    private String otpPostPichak;
    private Utility utility = new Utility();
    @Value("${shahkar.url}")
    private String shahkarUrl;
    @Value("${shahkar.identificationType}")
    private String identificationType;
    @Value("${shahkar.serviceType}")
    private String serviceType;

    @RequestMapping(
            value = {"/sendOtp"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String senOtp(@RequestBody String message, HttpServletRequest servletRequest) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            AAAServer.logger.info("****start****");
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String mobile = jsonObject.get("mobile").getAsString();
            String nationalId = jsonObject.get("nationalId").getAsString();
            Map<String, String> header = new HashMap();
            header.put("force", "true");
            header.put("Content-Type", "application/json");
            HttpParamMaker httpParamMaker = new HttpParamMaker();
            httpParamMaker.put("serviceNumber", mobile);
            httpParamMaker.put("identificationNo", nationalId);
            httpParamMaker.put("identificationType", this.identificationType);
            httpParamMaker.put("serviceType", this.serviceType);
            String shahkarResponse = this.utility.postCall(this.shahkarUrl, this.gson.toJson(httpParamMaker), header);
            JsonObject shahkarObject = (JsonObject)this.gson.fromJson(shahkarResponse, JsonObject.class);
            Integer code = shahkarObject.get("response").getAsInt();
            String shahkarResult = shahkarObject.get("result").getAsString();
            AAAServer.logger.info("code:" + code);
            AAAServer.logger.info("shahkarResult:" + shahkarResult);
            if (code == 200 && shahkarResult.equals("OK.")) {
                this.createOtherUser(mobile, nationalId);
                AAAServer.logger.info("befor otpService.sendOtp");
                OtpResponseDTO otpResponseDTO = otpService.sendOtp(mobile);
                SendSmsForSelectedMobileNumberMsg.Inbound inbound = new SendSmsForSelectedMobileNumberMsg.Inbound();
                inbound.setMobileNumber(mobile);
                StringBuilder sms = new StringBuilder(this.otpPreText);
                sms.append("\n");
                sms.append(otpResponseDTO.getOtp());
                sms.append("\n");
                String origin = servletRequest.getHeader("Origin");
                AAAServer.logger.info("origin:" + origin);
                if (origin != null && origin.contains("vekalati")) {
                    sms.append(this.otpPostVekalati);
                } else if (origin != null && origin.contains("pichak")) {
                    sms.append(this.otpPostPichak);
                } else {
                    sms.append(this.otpPostText);
                }

                inbound.setSmsMessage(sms.toString());
                String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "outputmanagement.message.sendSmsForSelectedobileNumberMsg", RequestType.TRANSACTION, inbound);
                Object result = this.lotusJmsService.receive(id, SendSmsForSelectedMobileNumberMsg.Outbound.class);
                AAAServer.logger.info(result);
                AAAServer.logger.debug(otpResponseDTO.toString(), new Supplier[]{() -> AAAServer.num});
                Map<String, String> map = new HashMap();
                map.put("remainingTimeInSeconds", String.valueOf(otpResponseDTO.getRemainingTimeInSeconds()));
                map.put("addedTime", String.valueOf(otpResponseDTO.getAddedTime()));
                responseMessage.setMessage(map);
            } else {
                throw new Exception("این شماره موبایل متعلق به شما نمی باشد");
            }
        } catch (ChannelManagerException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), this.errorCodeNull, this.errorCodeLatin);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    private void createOtherUser(String mobile, String nationalId) throws ChannelManagerException {
        AAAServer.logger.info("before user");
        com.caspian.pichak.model.entity.Users user = this.customUserDao.findByUsername(nationalId);
        AAAServer.logger.info("user:" + user);
        if (user == null) {
            CreateModernGatewayOtherUserMsg.Inbound inbound = new CreateModernGatewayOtherUserMsg.Inbound();
            VirtualOtherUserRequestBean virtualOtherUserRequestBean = new VirtualOtherUserRequestBean();
            virtualOtherUserRequestBean.setChannelType(ChannelType.MOBILE_BANK);
            virtualOtherUserRequestBean.setMobileNo(nationalId);
            virtualOtherUserRequestBean.setPlainPassword(mobile.concat(nationalId));
            virtualOtherUserRequestBean.setSelectedPasswordByUser(false);
            ChOtherUserInfoRequestBean chOtherUserInfoRequestBean = new ChOtherUserInfoRequestBean();
            chOtherUserInfoRequestBean.setGender(ChOtherUserGender.MALE);
            chOtherUserInfoRequestBean.setNationalCode(nationalId);
            chOtherUserInfoRequestBean.setMobileNo(mobile);
            virtualOtherUserRequestBean.setChOtherUserInfoRequestBean(chOtherUserInfoRequestBean);
            inbound.setRequestBean(virtualOtherUserRequestBean);
            CreateModernGatewayOtherUserMsg.Outbound outbound = (CreateModernGatewayOtherUserMsg.Outbound)this.provider.execute(inbound, CreateModernGatewayOtherUserMsg.Outbound.class);
            AAAServer.logger.info("befor authorityDao.findByName(ROLE_USER)");
            Authority authority = this.authorityDao.findByName("ROLE_USER");
            AAAServer.logger.info("after authorityDao.findByName(ROLE_USER)");
            user = new Users();
            user.setUserName(nationalId);
            user.setPassword(outbound.getResponseBean().getPassword());
            user.setEnabled(true);
            user.setAccountExpired(false);
            user.setAccountLocked(false);
            user.setCredentialsExpired(false);
            this.userDao.save(user);
            UserAuthority userAuthority = new UserAuthority();
            userAuthority.setAuthority(authority);
            userAuthority.setUsers(user);
            this.userAuthorityDao.save(userAuthority);
        }

    }

    @RequestMapping(
            value = {"/getMilitaryStatusInquiry"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST})
    @ResponseBody
    public String getMilitaryStatusInquiry(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            MilitaryStatusInquiryReportMsg.Inbound inbound = new MilitaryStatusInquiryReportMsg.Inbound();
            inbound.setNationalCode(nationalCode);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "cif.message.MilitaryStatusInquiryReportMsg", RequestType.INQUIRY, inbound);
            MilitaryStatusInquiryReportMsg.Outbound outbound = (lotusJmsService.receive(id, MilitaryStatusInquiryReportMsg.Outbound.class));
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getBeOpenedDeposits"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getBeOpenedDeposits(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            MGGetBeOpenedDepositsMsg.Inbound inbound = new MGGetBeOpenedDepositsMsg.Inbound();
            BeOpenedDepositDto beOpenedDepositDto = new BeOpenedDepositDto();
            inbound.setBeOpenedDepositDto(beOpenedDepositDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.GetBeOpenedDepositsRequestMsg", RequestType.INQUIRY, inbound);
            MGGetBeOpenedDepositsMsg.Outbound outbound = (MGGetBeOpenedDepositsMsg.Outbound)this.lotusJmsService.receive(id, MGGetBeOpenedDepositsMsg.Outbound.class);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getCustomerAddress"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCustomerAddress(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String cif = jsonObject.get("cif").getAsString();
            MGGetCustomerAddressMsg.Inbound inbound = new MGGetCustomerAddressMsg.Inbound();
            CustomerAddressDto customerAddressDto = new CustomerAddressDto();
            customerAddressDto.setCif(cif);
            inbound.setCustomerAddressDto(customerAddressDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.CustomerAddressRequestMsg", RequestType.INQUIRY, inbound);
            MGGetCustomerAddressMsg.Outbound outbound = (MGGetCustomerAddressMsg.Outbound)this.lotusJmsService.receive(id, MGGetCustomerAddressMsg.Outbound.class);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getOpenDeposit"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getOpenDeposit(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String cif = jsonObject.get("cif").getAsString();
            MGOpenDepositMsg.Inbound inbound = new MGOpenDepositMsg.Inbound();
            OpenDepositDto openDepositDto = new OpenDepositDto();
            openDepositDto.setCif(cif);
            inbound.setOpenDepositDto(openDepositDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.OpenDepositMsg", RequestType.INQUIRY, inbound);
            MGOpenDepositMsg.Outbound outbound = (MGOpenDepositMsg.Outbound)this.lotusJmsService.receive(id, MGOpenDepositMsg.Outbound.class);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getBouncedCheques"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getBouncedCheques(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            GetBouncedChequesByPersonInfoMsg.Inbound inbound = new GetBouncedChequesByPersonInfoMsg.Inbound();
            SamaInputPersonInfoDto samaInputPersonInfoDto = new SamaInputPersonInfoDto();
            samaInputPersonInfoDto.setPersonType(1);
            samaInputPersonInfoDto.setNationalId(nationalCode);
            inbound.setSamaInputPersonInfoDto(samaInputPersonInfoDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "deposit.samacheque.message.GetBouncedChequesByPersonInfoMsg", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/inquiryApplicantDebit"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String inquiryApplicantDebit(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            ApplicantDebitInquiryMsg.Inbound inbound = new ApplicantDebitInquiryMsg.Inbound();
            inbound.setNationalCode(nationalCode);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "lending.inquiry.applicant.debit", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/inquiryApplicantObligation"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String inquiryApplicantObligation(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            ApplicantObligationInquiryMsg.Inbound inbound = new ApplicantObligationInquiryMsg.Inbound();
            inbound.setNationalCode(nationalCode);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "lending.inquiry.applicant.obligation", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/inquiryApplicantQuarantor"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String inquiryApplicantQuarantor(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            ApplicantGuarantorInquiryMsg.Inbound inbound = new ApplicantGuarantorInquiryMsg.Inbound();
            inbound.setNationalCode(nationalCode);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "lending.inquiry.applicant.guarantor", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/virtualLoanCommission"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String virtualLoanCommission(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            BigDecimal amount = jsonObject.get("amount").getAsBigDecimal();
            int branchCode = jsonObject.get("branchCode").getAsInt();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            JsonElement description = jsonObject.get("Description");
            GetVirtualLoanCommissionMsg.Inbound inbound = new GetVirtualLoanCommissionMsg.Inbound();
            VirtualLoanCommissionDTO requestDto = new VirtualLoanCommissionDTO();
            requestDto.setAmount(amount);
            requestDto.setBranchCode(branchCode);
            requestDto.setDepositNumber(depositNumber);
            requestDto.setDescription(description != null ? description.getAsString() : "salam sosis");
            inbound.setRequestDto(requestDto);
            inbound.setUniqueTrackingCode(UUID.randomUUID().toString());
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "accounting.dealPosting.GetVirtualLoanCommissionMsg", RequestType.TRANSACTION, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getCoreDeposits"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCoreDeposits(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String cif = jsonObject.get("cif").getAsString();
            MGGetDepositsMsg.Inbound inbound = new MGGetDepositsMsg.Inbound();
            DepositSearchDto requestDto = new DepositSearchDto();
            requestDto.setCif(cif);
            inbound.setDepositSearchDto(requestDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.DepositSearchRequestMsg", RequestType.TRANSACTION, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getCustomerInfo"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCustomerInfo(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String nationalCode = jsonObject.get("customerNationalCode").getAsString();
            String nationalCodeType = jsonObject.get("customerNationalType").getAsString();
            NationalCodeType type = NationalCodeType.fromValue(nationalCodeType);
            MGCustomerExistenceInquiryMsg.Inbound inbound = new MGCustomerExistenceInquiryMsg.Inbound();
            ChCustomerExistenceInquiryRequestDto requestDto = new ChCustomerExistenceInquiryRequestDto();
            switch (type) {
                case INDIVIDUAL:
                    requestDto.setIndividualNationalCode(nationalCode);
                    break;
                case CORPORATE:
                    requestDto.setCorporateNationalCode(nationalCode);
                    break;
                case INDIVIDUAL_FOREIGNER:
                    requestDto.setIndividualForeignerNo(nationalCode);
                    break;
                case CORPORATE_FOREIGNER:
                    requestDto.setCorporateForeignerNo(nationalCode);
            }

            inbound.setRequestDto(requestDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGCustomerExistenceInquiryMsg", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            JsonObject customerExistenceInquiry = (JsonObject)this.gson.fromJson(outbound, JsonObject.class);
            responseMessage.setMessage(customerExistenceInquiry);
        } catch (CoreException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getMessage());
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            path = {"/getCustomerInfoByPan"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getCustomerInfoByPan(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String pan = jsonObject.get("pan").getAsString();
            LoadCardCustomerInfoByNumberMsg.Inbound inbound = new LoadCardCustomerInfoByNumberMsg.Inbound();
            ChLoadCardCustomerInfoByNumberRequestBean requestBean = new ChLoadCardCustomerInfoByNumberRequestBean();
            requestBean.setCardNumber(pan);
            inbound.setRequestBean(requestBean);
            LoadCardCustomerInfoByNumberMsg.Outbound outbound = (LoadCardCustomerInfoByNumberMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, LoadCardCustomerInfoByNumberMsg.Outbound.class);
            ChCardCustomerInfoResponseBean chCardCustomerInfoResponseBean = outbound.getResponseBean().getChCardCustomerInfoResponseBean();
            List<ChContactInfoBean> chContactInfoBeanList = outbound.getResponseBean().getChContactInfoBeanList();
            String mobile = "";
            Iterator var13 = chContactInfoBeanList.iterator();
            if (var13.hasNext()) {
                ChContactInfoBean chContactInfoBean = (ChContactInfoBean)var13.next();
                mobile = chContactInfoBean.getContactValue();
            }

            this.createOtherUser(mobile, chCardCustomerInfoResponseBean.getNationalCode());
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            path = {"/getCustomerInformation"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getCustomerInformation(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            long cif = jsonObject.get("customerNumber").getAsLong();
            GetCustomerInformationMsg.Inbound inbound = new GetCustomerInformationMsg.Inbound();
            inbound.setCustomerNumber(cif);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "lending.generalFile.GetCustomerInformationMsg", RequestType.TRANSACTION, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            path = {"/getCustomerInfoByPanCore"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getCustomerInfoByPanCore(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String pan = jsonObject.get("pan").getAsString();
            MGLoadCardCustomerInfoByNumberMsg.Inbound inbound = new MGLoadCardCustomerInfoByNumberMsg.Inbound();
            inbound.setCardNumber(pan);
//            inbound.setCardNumber("6221061023186077");
            inbound.setValidateCvv2AndExpireDate(false);
            String id = lotusJmsService.send(coreUsername, coreBranchcode, "channelManagement.MGLoadCardCustomerInfoByNumber", RequestType.TRANSACTION, inbound);
            String outbound = lotusJmsService.receive(id);
            JsonObject responseObj = gson.fromJson(outbound, JsonObject.class);
            JsonObject requestMsg = new JsonObject();
            JsonObject cardCustomerInfoResponseDto = responseObj.get("cardCustomerInfoResponseDto").getAsJsonObject();
            requestMsg.add("customerNumber", cardCustomerInfoResponseDto.get("customerId"));
            String customerInformation = this.getCustomerInformation(gson.toJson(requestMsg), request);
            JsonObject customerInformationObj = gson.fromJson(customerInformation, JsonObject.class);
            JsonElement customerInformationMessage = gson.fromJson(customerInformationObj.get("message").getAsString(), JsonElement.class);
            JsonObject messageObj = gson.fromJson(customerInformationMessage.getAsString(), JsonObject.class);
            JsonObject customerDTO = messageObj.get("customerDTO").getAsJsonObject();
            responseObj.add("shahabCode", customerDTO.get("shahabCode"));
            responseMessage.setMessage(responseObj);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getMGCustomerDefaultMobile"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getMgCustomerDefaultMobile(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            long cif = jsonObject.get("cif").getAsLong();
            MGGetAllCustomerMobileNumbersMsg.Inbound inbound = new MGGetAllCustomerMobileNumbersMsg.Inbound();
            inbound.setCustomerRef(cif);
            String id = lotusJmsService.send(this.coreUsername, this.coreBranchcode, "message.card.MGGetAllCustomerMobileNumbersMsg", RequestType.TRANSACTION, inbound);
            String outbound = lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            value = {"/getSetting"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getSetting(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            JsonElement keyElement = jsonObject.get("key");
            if (keyElement != null) {
                responseMessage.setMessage(settingRepository.findByKey(keyElement.getAsString()));
            } else {
                responseMessage.setMessage(settingRepository.findAllActive());
            }
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }
}
