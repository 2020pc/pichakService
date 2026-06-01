package com.caspian.pichak.ResourceServer;


import com.caspian.banking.ebanking.dto.*;
import com.caspian.banking.ebanking.dto.ChPinType;
import com.caspian.banking.ebanking.dto.deposit.pichak.*;
import com.caspian.banking.ebanking.message.*;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakAcceptChequeMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakInquiryChequeMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakIssueChequeMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakTransferChequeMsg;
import com.caspian.banking.model.dto.ChCustomerExistenceInquiryRequestDto;
import com.caspian.banking.model.messages.MGCustomerExistenceInquiryMsg;
import com.caspian.banking.model.messages.MGIbanInquiryServiceMsg;
import com.caspian.banking.util.DateUtil;
import com.caspian.moderngateway.core.channelmanagerinfrastructure.exception.ChannelManagerException;
import com.caspian.moderngateway.core.channelmanagerinfrastructure.exception.InvalidSessionException;
import com.caspian.moderngateway.core.channelmanagerinfrastructure.util.ChannelType;
import com.caspian.moderngateway.core.channelmanagerserviceaggregator.dto.pol.ChCreditorPartyIdentificationBean;
import com.caspian.moderngateway.core.channelmanagerserviceaggregator.dto.pol.ChDebtorPartyIdentificationBean;
import com.caspian.moderngateway.core.channelmanagerserviceaggregator.dto.pol.ChPaymentOrderRqBean;
import com.caspian.moderngateway.core.channelmanagerserviceaggregator.dto.pol.ChPostPaymentOrderRequestBean;
import com.caspian.moderngateway.core.coreservice.dto.*;
import com.caspian.moderngateway.core.coreservice.dto.ChBlockedChequeReasonType;
import com.caspian.moderngateway.core.coreservice.dto.ChCardTransactionType;
import com.caspian.moderngateway.core.coreservice.dto.ChDepositGroupType;
import com.caspian.moderngateway.core.coreservice.dto.ChDepositStatus;
import com.caspian.moderngateway.core.coreservice.dto.ChInaugurateDepositDurationUnit;
import com.caspian.moderngateway.core.coreservice.dto.ChLoanPaymentMethod;
import com.caspian.moderngateway.core.coreservice.dto.ChSignatureOwnerStatus;
import com.caspian.moderngateway.core.coreservice.dto.ChWithdrawalOption;
import com.caspian.moderngateway.core.coreservice.dto.account.ChCloseAccountRequestBean;
import com.caspian.moderngateway.core.coreservice.dto.card.*;
import com.caspian.moderngateway.core.coreservice.dto.otheruser.ChOtherUserLoginRequestBean;
import com.caspian.moderngateway.core.domainmodel.dto.dashboard.ChDashboardFavouriteAccountBean;
import com.caspian.moderngateway.core.domainmodel.dto.dashboard.ChDashboardFavouritePanBean;
import com.caspian.moderngateway.core.domainmodel.dto.dashboard.ChDashboardUserFavouriteBean;
import com.caspian.moderngateway.core.domainmodel.dto.dashboard.ChPersistDashboardUserFavouriteRequestBean;
import com.caspian.moderngateway.core.domainmodel.dto.internetPackages.ChOperatorTypes;
import com.caspian.moderngateway.core.domainmodel.dto.internetPackages.ChPackageTypes;
import com.caspian.moderngateway.core.domainmodel.dto.internetPackages.ChSearchInternetPackagesRequestBean;
import com.caspian.moderngateway.core.domainmodel.dto.user.ModernGatewayUserInfoRequestBean;
import com.caspian.moderngateway.core.message.*;
import com.caspian.moderngateway.core.message.account.CloseAccountMsg;
import com.caspian.moderngateway.core.message.boursar.carexchange.CarExchangeActivateAssignCustomerAccountMsg;
import com.caspian.moderngateway.core.message.boursar.carexchange.CarExchangeAssignCustomerAccountMsg;
import com.caspian.moderngateway.core.message.boursar.carexchange.CarExchangeDeactivateAssignCustomerAccountMsg;
import com.caspian.moderngateway.core.message.boursar.carexchange.CarExchangeGetAssignedCustomerAccountsMsg;
import com.caspian.moderngateway.core.message.boursar.dto.carexchange.ChCarExchangeActivateAssignCustomerAccountRequestBean;
import com.caspian.moderngateway.core.message.boursar.dto.carexchange.ChCarExchangeAssignCustomerAccountRequestBean;
import com.caspian.moderngateway.core.message.boursar.dto.carexchange.ChCarExchangeDeactivateAssignCustomerAccountRequestBean;
import com.caspian.moderngateway.core.message.card.*;
import com.caspian.moderngateway.core.message.cusomerinfopriority.GetCustomerAllCardsPriorityMsg;
import com.caspian.moderngateway.core.message.dashboard.PersistDashboardUserFavouriteMsg;
import com.caspian.moderngateway.core.message.internetPackages.SearchInternetPackagesMsg;
import com.caspian.moderngateway.core.message.otheruser.LoginStaticOtherUserMsg;
import com.caspian.moderngateway.core.message.pol.PostPaymentOrderMsg;
import com.caspian.moderngateway.core.message.switchservice.TopupChargeFromVirtualTerminalMsg;
import com.caspian.moderngateway.core.security.dto.ChSecondPasswordType;
import com.caspian.moderngateway.core.switchservice.dto.ChTerminalType;
import com.caspian.moderngateway.core.switchservice.dto.ChTopupChargeRequest;
import com.caspian.moderngateway.core.switchservice.dto.TopupServiceCode;
import com.caspian.moderngateway.infrastructurespi.common.message.ChMessageHeader;
import com.caspian.pichak.exceptions.CoreException;
import com.caspian.pichak.exceptions.PichakException;
import com.caspian.pichak.model.dto.PichakError;
import com.caspian.pichak.model.dto.ResponseMessage;
import com.caspian.pichak.model.entity.Users;
import com.caspian.pichak.service.lotus.RequestType;
import com.caspian.pichak.type.ClientType;
import com.caspian.pichak.type.NationalCodeType;
import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.utility.Util;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.pb.ouc.util.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping({"/OTP/digital/user"})
public class UserResource extends BaseResource {
    private static String sessionId;
    private final Utility utility = new Utility();
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    @Value("${SOFTWARE_AGENT_USER}")
    protected String softwareAgentUser;
    @Value("${SOFTWARE_AGENT_PASS}")
    protected String softwareAgentPassword;

//    @RequestMapping(
//            value = {"/revokeToken"},
//            method = {RequestMethod.POST}
//    )
//    @ResponseStatus(HttpStatus.OK)
//    public void logout(HttpServletRequest request) {
//        TokenStore tokenStore = new JdbcTokenStore(this.dataSource);
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null) {
//            String tokenValue = authHeader.replace("Bearer", "").trim();
//            OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
//            tokenStore.removeAccessToken(accessToken);
//            if (accessToken.getRefreshToken() != null) {
//                tokenStore.removeRefreshToken(accessToken.getRefreshToken());
//            }
//        }
//
//    }

    @RequestMapping(
            value = {"/registerUser"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String verifyOtp(@RequestBody String message, HttpServletRequest servletRequest) {
        try {
            this.otpService.sendOtp(message);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            ;
        }
    }

    @RequestMapping(
            value = {"/login"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String login(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String nationalId = jsonObject.get("nationalId").getAsString();
            Users users = this.userDao.findByUsername(nationalId);
            LoginStaticOtherUserMsg.Inbound inbound = new LoginStaticOtherUserMsg.Inbound();
            ChOtherUserLoginRequestBean chOtherUserLoginRequestBean = new ChOtherUserLoginRequestBean();
            chOtherUserLoginRequestBean.setUsername(users.getUserName());
            chOtherUserLoginRequestBean.setPassword(users.getPassword());
            chOtherUserLoginRequestBean.setSelectedPasswordByUser(false);
            chOtherUserLoginRequestBean.setLoadUserActivities(true);
            inbound.setRequestBean(chOtherUserLoginRequestBean);
            LoginStaticOtherUserMsg.Outbound outbound = (LoginStaticOtherUserMsg.Outbound)this.provider.execute(inbound, LoginStaticOtherUserMsg.Outbound.class);
            responseMessage.setMessage(outbound.getChLoginResponseBean().getSessionId());
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
            value = {"/loginByCard"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String loginByCard(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String pan = jsonObject.get("pan").getAsString();
            String pin = jsonObject.get("pin").getAsString();
            LoginByCardMsg.Inbound inbound = new LoginByCardMsg.Inbound();
            ChLoginByCardRequestBean requestBean = new ChLoginByCardRequestBean();
            requestBean.setPan(pan);
            inbound.setRequestBean(requestBean);
            LoginByCardMsg.Outbound outbound = (LoginByCardMsg.Outbound)this.provider.execute(Util.getChMessageHeader(ChMessageHeader.ChannelServiceType.INTERNET_BANK.name()), inbound, LoginByCardMsg.Outbound.class);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/setUserLogin"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String SetUserLogin(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            JsonElement username = jsonObject.get("username");
            JsonElement password = jsonObject.get("password");
            SetModernGatewayUserLoginPasswordMsg.Inbound inbound = new SetModernGatewayUserLoginPasswordMsg.Inbound();
            inbound.setUsername(username.getAsString());
            inbound.setPassword(password.getAsString());
            inbound.setChannelType(ChannelType.PERSONAL_INTERNET_BANK);
            SetModernGatewayUserLoginPasswordMsg.Outbound outbound = (SetModernGatewayUserLoginPasswordMsg.Outbound)this.provider.execute(Util.getChMessageHeader(ChMessageHeader.ChannelServiceType.INTERNET_BANK.name()), inbound, SetModernGatewayUserLoginPasswordMsg.Outbound.class);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/setCardAuthentication"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String setCardHolderAuthentication(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            JsonElement pin = jsonObject.get("pin");
            JsonElement pan = jsonObject.get("pan");
            JsonElement cvv2 = jsonObject.get("cvv2");
            JsonElement expireDate = jsonObject.get("expireDate");
            MGLoginCardByPinMsg.Inbound inbound = new MGLoginCardByPinMsg.Inbound();
            inbound.setPin(pin.getAsString());
            inbound.setCardNumber(pan.getAsString());
            inbound.setPinType(ChPinType.EPAY);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGLoginCardByPinMsg", RequestType.INQUIRY, inbound);
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
            value = {"/activateUser"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String activateUser(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            JsonElement username = jsonObject.get("username");
            JsonElement channelType = jsonObject.get("channelType");
            ActivateModernGatewayUserMsg.Inbound inbound = new ActivateModernGatewayUserMsg.Inbound();
            inbound.setUsername(username.getAsString());
            inbound.setChannelType(ChannelType.valueOf(channelType.getAsString()));
            ActivateModernGatewayUserMsg.Outbound outbound = (ActivateModernGatewayUserMsg.Outbound)this.provider.execute(inbound, ActivateModernGatewayUserMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/deactivateUser"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String deactivateUser(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            JsonElement username = jsonObject.get("username");
            JsonElement sessionId = jsonObject.get("sessionId");
            DeactivateModernGatewayUserMsg.Inbound inbound = new DeactivateModernGatewayUserMsg.Inbound();
            inbound.setUsername(username.getAsString());
            inbound.setChannelType(ChannelType.PERSONAL_INTERNET_BANK);
            DeactivateModernGatewayUserMsg.Outbound outbound = (DeactivateModernGatewayUserMsg.Outbound)this.provider.execute(Util.getChMessageHeader(ChMessageHeader.ChannelServiceType.INTERNET_BANK.name()), inbound, DeactivateModernGatewayUserMsg.Outbound.class, sessionId.getAsString());
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getModernUserInfo"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST})
    @ResponseBody
    public String getModernUserInfo(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            JsonElement username = jsonObject.get("username");
            GetModernGatewayUserInfoMsg.Inbound inbound = new GetModernGatewayUserInfoMsg.Inbound();
            ModernGatewayUserInfoRequestBean requestBean = new ModernGatewayUserInfoRequestBean();
            requestBean.setChannelType(ChannelType.PERSONAL_INTERNET_BANK);
            requestBean.setLoadActivities(false);
            requestBean.setReferenceCode(username.getAsString());
            inbound.setRequestBean(requestBean);
            GetModernGatewayUserInfoMsg.Outbound outbound = (GetModernGatewayUserInfoMsg.Outbound)this.provider.execute(Util.getChMessageHeader(ChMessageHeader.ChannelServiceType.INTERNET_BANK.name()), inbound, GetModernGatewayUserInfoMsg.Outbound.class);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/getBillInfo"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"})
    public String getBillInfo(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement billId = jsonObject.get("billId");
            JsonElement payIdId = jsonObject.get("payId");
            GetBillInfoMsg.Inbound inbound = new GetBillInfoMsg.Inbound();
            ChBillInfoSearchRequestBean requestBean = new ChBillInfoSearchRequestBean();
            requestBean.setBillId(billId.getAsString());
            requestBean.setPayId(payIdId.getAsString());
            inbound.setRequestBean(requestBean);
            GetBillInfoMsg.Outbound outbound = provider.execute(Util.getChMessageHeader(request), inbound, GetBillInfoMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/persistDepositAlias"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String persistDepositAliasMsg(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement depositNumber = jsonObject.get("accountNumber");
            JsonElement depositAlias = jsonObject.get("depositAlias");
            PersistDepositAliasMsg.Inbound inbound = new PersistDepositAliasMsg.Inbound();
            ChDepositAliasRequestBean chDepositAliasRequestBean = new ChDepositAliasRequestBean();
            chDepositAliasRequestBean.setDepositNumber(depositNumber.getAsString());
            chDepositAliasRequestBean.setDepositAlias(depositAlias.getAsString());
            inbound.setChDepositAliasRequestBean(chDepositAliasRequestBean);
            PersistDepositAliasMsg.Outbound outbound = provider.execute(Util.getChMessageHeader(request), inbound, PersistDepositAliasMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/payBill"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"})
    public String payBill(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement billId = jsonObject.get("billId");
            JsonElement payId = jsonObject.get("payId");
            JsonElement pan = jsonObject.get("pan");
            JsonElement pin = jsonObject.get("pin");
            JsonElement expDate = jsonObject.get("expDate");
            JsonElement cvv2 = jsonObject.get("cvv2");
            JsonElement pinType = jsonObject.get("pinType");
            PayBillMsg.Inbound inbound = new PayBillMsg.Inbound();
            ChBillPaymentRequestBean requestBean = new ChBillPaymentRequestBean();
            ChCardAuthorizeParamsBean cardAuthorizeParams = new ChCardAuthorizeParamsBean();
            cardAuthorizeParams.setPin(pin.getAsString());
            cardAuthorizeParams.setCvv2(cvv2.getAsString());
            cardAuthorizeParams.setExpDate(expDate.getAsString());
            cardAuthorizeParams.setPinType(com.caspian.moderngateway.core.coreservice.dto.ChPinType.fromValue(pinType.getAsString()));
            requestBean.setBillId(billId.getAsString());
            requestBean.setPayId(payId.getAsString());
            requestBean.setMobileNo("09331930900");
            requestBean.setCardAuthorizeParams(cardAuthorizeParams);
            requestBean.setPan(pan.getAsString());
            requestBean.setRequireVerification(false);
            inbound.setRequestBean(requestBean);
            PayBillMsg.Outbound outbound = this.provider.execute(Util.getChMessageHeader(request), inbound, PayBillMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/chargeBuyTopUp"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String chargeBuyTopUp(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement amount = jsonObject.get("amount");
            JsonElement mobileNumber = jsonObject.get("mobileNumber");
            JsonElement topupServiceCode = jsonObject.get("topupServiceCode");
            JsonElement pan = jsonObject.get("pan");
            JsonElement pin = jsonObject.get("pin");
            JsonElement expDate = jsonObject.get("expDate");
            JsonElement cvv2 = jsonObject.get("cvv2");
            JsonElement terminalType = jsonObject.get("terminalType");
            TopupChargeFromVirtualTerminalMsg.Inbound inbound = new TopupChargeFromVirtualTerminalMsg.Inbound();
            ChTopupChargeRequest requestBean = new ChTopupChargeRequest();
            requestBean.setAmount(amount.getAsBigDecimal());
            requestBean.setChTopupServiceCode(TopupServiceCode.valueOf(topupServiceCode.getAsString()));
            requestBean.setMobileNo(mobileNumber.getAsString());
            requestBean.setCardNo(pan.getAsString());
            requestBean.setPin(pin.getAsString());
            requestBean.setCvv2(cvv2.getAsString());
            String date = expDate.getAsString();
            requestBean.setExpireDateMonth(Integer.valueOf(date.substring(2)));
            requestBean.setExpireDateYear(Integer.valueOf(date.substring(0, 1)));
            requestBean.setTerminalType(ChTerminalType.valueOf(terminalType.getAsString()));
            inbound.setRequest(requestBean);
            TopupChargeFromVirtualTerminalMsg.Outbound outbound = (TopupChargeFromVirtualTerminalMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, TopupChargeFromVirtualTerminalMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/searchInternetPackages"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String searchInternetPackages(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String operatorTypes = jsonObject.get("operatorTypes").getAsString();
            String packageTypes = jsonObject.get("packageTypes").getAsString();
            SearchInternetPackagesMsg.Inbound inbound = new SearchInternetPackagesMsg.Inbound();
            ChSearchInternetPackagesRequestBean requestBean = new ChSearchInternetPackagesRequestBean();
            requestBean.setInternetPackageOperator(ChOperatorTypes.valueOf(operatorTypes));
            requestBean.setPackageType(ChPackageTypes.valueOf(packageTypes));
            inbound.setRequestBean(requestBean);
            SearchInternetPackagesMsg.Outbound outbound = (SearchInternetPackagesMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, SearchInternetPackagesMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/getLoanDetail"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getLoanDetail(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement cbLoanNumber = jsonObject.get("cbLoanNumber");
            JsonElement loanNumber = jsonObject.get("loanNumber");
            JsonElement length = jsonObject.get("length");
            JsonElement offset = jsonObject.get("offset");
            GetLoanDetailMsg.Inbound inbound = new GetLoanDetailMsg.Inbound();
            ChLoanDetailSearchRequestBean requestBean = new ChLoanDetailSearchRequestBean();
            requestBean.setHasDetail(true);
            if (cbLoanNumber != null) {
                requestBean.setCbLoanNumber(cbLoanNumber.getAsString());
            }

            if (loanNumber != null) {
                requestBean.setLoanNumber(loanNumber.getAsString());
            }

            if (length != null) {
                requestBean.setLength(length.getAsLong());
            }

            if (offset != null) {
                requestBean.setOffset(offset.getAsLong());
            }

            inbound.setRequestBean(requestBean);
            GetLoanDetailMsg.Outbound outbound = (GetLoanDetailMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetLoanDetailMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/payLoan"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String payLoan(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String loanNumber = jsonObject.get("loanNumber").getAsString();
            String customDepositNumber = jsonObject.get("customDepositNumber").getAsString();
            Long amount = jsonObject.get("amount").getAsLong();
            PayLoanMsg.Inbound inbound = new PayLoanMsg.Inbound();
            ChLoanPaymentRequestBean requestBean = new ChLoanPaymentRequestBean();
            requestBean.setLoanNumber(loanNumber);
            requestBean.setAmount(new BigDecimal(amount));
            requestBean.setCustomDepositNumber(customDepositNumber);
            requestBean.setSecondPasswordNecessity(false);
            requestBean.setPaymentMethod(ChLoanPaymentMethod.CUSTOM_DEPOSIT);
            inbound.setRequestBean(requestBean);
            PayLoanMsg.Outbound outbound = (PayLoanMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, PayLoanMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/getLoans"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getLoans(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetLoansMsg.Inbound inbound = new GetLoansMsg.Inbound();
            ChLoansSearchRequestBean requestBean = new ChLoansSearchRequestBean();
            inbound.setRequestBean(requestBean);
            GetLoansMsg.Outbound outbound = provider.execute(Util.getChMessageHeader(request), inbound, GetLoansMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getDepositsChannel"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getDepositsChannel(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetDepositsMsg.Inbound inbound = new GetDepositsMsg.Inbound();
            ChDepositSearchRequestBean requestBean = new ChDepositSearchRequestBean();
            List<ChDepositGroupType> includeType = new ArrayList();

            for(String s : this.vekalatiDepositTypeList) {
                includeType.add(ChDepositGroupType.valueOf(s));
            }

            requestBean.setIncludeType(includeType);
            requestBean.setDepositStatus(ChDepositStatus.OPEN);
            List<ChSignatureOwnerStatus> signatureStatus = new ArrayList();
            signatureStatus.add(ChSignatureOwnerStatus.OWNER_OF_DEPOSIT_AND_SIGNATURE);
            requestBean.setSignatureStatus(signatureStatus);
            inbound.setChDepositSearchRequestBean(requestBean);
            GetDepositsMsg.Outbound outbound = provider.execute(Util.getChMessageHeader(request), inbound, GetDepositsMsg.Outbound.class, sessionId);
            ChDepositResponseBean chDepositResponseBean = outbound.getChDepositResponseBean();
            List<ChDepositBean> depositBeans = chDepositResponseBean.getDepositBeans();
            depositBeans.removeIf((chDepositBean) -> chDepositBean.getWithdrawalOption() != ChWithdrawalOption.BE_TANHAYEE);
            JsonArray depositBeanArray = gson.fromJson(gson.toJson(depositBeans), JsonArray.class);
            JsonObject depositResponseBean = new JsonObject();
            depositResponseBean.add("depositBeans", depositBeanArray);
            JsonObject response = new JsonObject();
            response.add("chDepositResponseBean", depositResponseBean);
            responseMessage.setMessage(response);
        } catch (ChannelManagerException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), errorCodeNull, errorCodeLatin);
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
            value = {"/getDeposits"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getDeposits(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            Util util = new Util();
            String loginUserInfo = util.getUsernameFromJwtToken(request);
            JsonObject object = new JsonObject();
            object.addProperty("nationalCode", loginUserInfo.split("\\|")[0]);
            object.addProperty("nationalCodeType", "INDIVIDUAL");
            String customerInfo = getCustomerInfo(gson.toJson(object), request);
            JsonObject customerObject = gson.fromJson(customerInfo, JsonObject.class);
            JsonObject messageObject = gson.fromJson(customerObject.get("message").getAsString(), JsonObject.class);
            JsonObject responseDtoObject = messageObject.get("responseDto").getAsJsonObject();
            String customerNo = responseDtoObject.get("customerNo").getAsString();
            DepositSearchDto requestDto = new DepositSearchDto();
            List<com.caspian.banking.ebanking.dto.ChDepositGroupType> includeType = new ArrayList();

            for(String s : vekalatiDepositTypeList) {
                includeType.add(com.caspian.banking.ebanking.dto.ChDepositGroupType.valueOf(s));
            }

            requestDto.setIncludeType(includeType);
            List<String> includeCurrency = new ArrayList<>();
            includeCurrency.add("IRR");
            requestDto.setIncludeCurrency(includeCurrency);
            requestDto.setDepositStatus(com.caspian.banking.ebanking.dto.ChDepositStatus.OPEN);
            List<com.caspian.banking.ebanking.dto.ChSignatureOwnerStatus> signatureStatus = new ArrayList();
            signatureStatus.add(com.caspian.banking.ebanking.dto.ChSignatureOwnerStatus.OWNER_OF_DEPOSIT_AND_SIGNATURE);
            requestDto.setSignatureStatus(signatureStatus);
            MGGetDepositsMsg.Inbound inbound = new MGGetDepositsMsg.Inbound();
            requestDto.setCif(customerNo);
            inbound.setDepositSearchDto(requestDto);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.DepositSearchRequestMsg", RequestType.TRANSACTION, inbound);
            String outbound = this.lotusJmsService.receive(id);
            JsonObject outboundObject = (JsonObject)this.gson.fromJson(outbound, JsonObject.class);
            JsonArray depositBeanArray = outboundObject.get("depositBeans").getAsJsonArray();
            JsonObject depositResponseBean = new JsonObject();
            depositResponseBean.add("depositBeans", depositBeanArray);
            JsonObject response = new JsonObject();
            response.add("chDepositResponseBean", depositResponseBean);
            responseMessage.setMessage(response);
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
            value = {"/getCards"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCards(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetCustomerAllCardsPriorityMsg.Inbound inbound = new GetCustomerAllCardsPriorityMsg.Inbound();
            ChGetCardWithAmountRequestBean chGetCardWithAmountRequestBean = new ChGetCardWithAmountRequestBean();
            inbound.setChGetCardWithAmountRequestBean(chGetCardWithAmountRequestBean);
            GetCustomerAllCardsPriorityMsg.Outbound outbound = (GetCustomerAllCardsPriorityMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetCustomerAllCardsPriorityMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
        } catch (ChannelManagerException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(),errorCodeNull, errorCodeLatin);
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
            value = {"/getBranch"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getBranch(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement code = jsonObject.get("code");
            GetBranchesMsg.Inbound inbound = new GetBranchesMsg.Inbound();
            ChBranchSearchRequestBean requestBean = new ChBranchSearchRequestBean();
            if (code != null) {
                requestBean.setCode(code.getAsString());
            }

            inbound.setRequestBean(requestBean);
            GetBranchesMsg.Outbound outbound = (GetBranchesMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetBranchesMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getFavourite"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getFavourite(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetUserContactWithContactDetailsMsg.Inbound inbound = new GetUserContactWithContactDetailsMsg.Inbound();
            GetUserContactWithContactDetailsMsg.Outbound outbound = (GetUserContactWithContactDetailsMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetUserContactWithContactDetailsMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/deleteFavourite"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String deleteFavourite(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            RemoveContactMsg.Inbound inbound = new RemoveContactMsg.Inbound();
            inbound.setId(123456L);
            GetUserContactWithContactDetailsMsg.Outbound outbound = (GetUserContactWithContactDetailsMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetUserContactWithContactDetailsMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getShetabOtpSms"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getShetabOtpSmsV2Msg(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            String pan = jsonObject.get("pan").getAsString();
            String payee = jsonObject.get("payee").getAsString();
            ShetabOtpSmsV2Msg.Inbound inbound = new ShetabOtpSmsV2Msg.Inbound();
            ChShetabOtpSmsV2RequestBean requestBean = new ChShetabOtpSmsV2RequestBean();
            requestBean.setAmount(new BigDecimal(amount));
            requestBean.setPan(pan);
            requestBean.setTransactionType(ChShetabOtpSmsV2TransactionType.FUND_TRANSFER_FROM);
            requestBean.setCardAcceptorName(payee);
            requestBean.setPayee(payee);
            requestBean.setTrackingNumber((new Random()).nextInt());
            inbound.setRequestBean(requestBean);
            ShetabOtpSmsV2Msg.Outbound outbound = provider.execute(Util.getChMessageHeader(request), inbound, ShetabOtpSmsV2Msg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
        } catch (ChannelManagerException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), errorCodeNull, errorCodeLatin);
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
            value = {"/setFavourite"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String setFavourite(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            PersistDashboardUserFavouriteMsg.Inbound inbound = new PersistDashboardUserFavouriteMsg.Inbound();
            ChPersistDashboardUserFavouriteRequestBean requestBean = new ChPersistDashboardUserFavouriteRequestBean();
            ChDashboardUserFavouriteBean chDashboardUserFavouriteBean = new ChDashboardUserFavouriteBean();
            ChDashboardFavouriteAccountBean chDashboardFavouriteAccountBean = new ChDashboardFavouriteAccountBean();
            chDashboardFavouriteAccountBean.setFavouriteAccountNumber("80001148191008");
            chDashboardUserFavouriteBean.setPersistAllWidgetsForAccountNumber(true);
            chDashboardUserFavouriteBean.setChDashboardFavouriteAccountBean(chDashboardFavouriteAccountBean);
            chDashboardUserFavouriteBean.setChDashboardFavouriteAccountBean(chDashboardFavouriteAccountBean);
            ChDashboardFavouritePanBean chDashboardFavouritePanBean = new ChDashboardFavouritePanBean();
            chDashboardFavouritePanBean.setFavouritePan("6221061104330651");
            chDashboardUserFavouriteBean.setPersistAllWidgetsForPan(true);
            chDashboardUserFavouriteBean.setChDashboardFavouritePanBean(chDashboardFavouritePanBean);
            List<ChDashboardUserFavouriteBean> chDashboardUserFavouriteBeanList = new ArrayList();
            chDashboardUserFavouriteBeanList.add(chDashboardUserFavouriteBean);
            requestBean.setChDashboardUserFavouriteBeanList(chDashboardUserFavouriteBeanList);
            inbound.setRequestBean(requestBean);
            PersistDashboardUserFavouriteMsg.Outbound outbound = (PersistDashboardUserFavouriteMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, PersistDashboardUserFavouriteMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getUserInfo"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getUserInfo(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetUserInfoMsg.Inbound inbound = new GetUserInfoMsg.Inbound();
            GetUserInfoMsg.Outbound outbound = (GetUserInfoMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetUserInfoMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getCustomerInfos"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCustomerInfos(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetCustomerExtraDataMsg.Inbound inbound = new GetCustomerExtraDataMsg.Inbound();
            ChCustomerExtraDataRequestBean requestBean = new ChCustomerExtraDataRequestBean();
            inbound.setRequestBean(requestBean);
            GetCustomerExtraDataMsg.Outbound outbound = (GetCustomerExtraDataMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetCustomerExtraDataMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/cardToDepositTransfer"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String CardToDepositTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String secondPassword = jsonObject.get("secondPassword").getAsString();
            String cardNumber = jsonObject.get("cardNumber").getAsString();
            String destinationAccount = jsonObject.get("destinationAccount").getAsString();
            BigDecimal amount = jsonObject.get("amount").getAsBigDecimal();
            PrepaidCardTransferMsg.Inbound inbound = new PrepaidCardTransferMsg.Inbound();
            ChPrepaidCardTransferRequestBean requestBean = new ChPrepaidCardTransferRequestBean();
            requestBean.setAmount(amount);
            requestBean.setSecondPassword(secondPassword);
            requestBean.setCardNumber(cardNumber);
            requestBean.setDestinationAccount(destinationAccount);
            requestBean.setCheckUniqueTrackingCode(false);
            inbound.setRequestBean(requestBean);
            PrepaidCardTransferMsg.Outbound outbound = (PrepaidCardTransferMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, PrepaidCardTransferMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/accountTransfer"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String accountTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String secondPassword = jsonObject.get("secondPassword").getAsString();
            String fromDeposit = jsonObject.get("fromDeposit").getAsString();
            String toDeposit = jsonObject.get("toDeposit").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            NormalTransferMsg.Inbound inbound = new NormalTransferMsg.Inbound();
            GetDepositsOwnerNameMsg.Inbound inboundAcc = new GetDepositsOwnerNameMsg.Inbound();
            ChDepositsOwnerRequestBean requestBean = new ChDepositsOwnerRequestBean();
            requestBean.setDepositNumbers(Arrays.asList(toDeposit));
            inboundAcc.setRequestBean(requestBean);
            GetDepositsOwnerNameMsg.Outbound outboundAcc = (GetDepositsOwnerNameMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inboundAcc, GetDepositsOwnerNameMsg.Outbound.class, sessionId);
            ChNormalTransferRequestBean normalTransferRequestBean = new ChNormalTransferRequestBean();
            normalTransferRequestBean.setAmount(new BigDecimal(amount));
            normalTransferRequestBean.setSourceDeposit(fromDeposit);
            normalTransferRequestBean.setDestinationDeposit(toDeposit);
            normalTransferRequestBean.setChSecondPasswordType(ChSecondPasswordType.TRANSFER_SECOND_PASS);
            normalTransferRequestBean.setSecondPassword(secondPassword);
            inbound.setNormalTransferRequestBean(normalTransferRequestBean);
            NormalTransferMsg.Outbound outbound = (NormalTransferMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, NormalTransferMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/otpAccountTransfer"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String otpAccountTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String accountNumber = jsonObject.get("accountNumber").getAsString();
            String destinationNumber = jsonObject.get("destinationNumber").getAsString();
            BigDecimal amount = jsonObject.get("amount").getAsBigDecimal();
            String chAccountTransferType = jsonObject.get("chAccountTransferType").getAsString();
            OtpGenerateForAccountTransferMsg.Inbound inbound = new OtpGenerateForAccountTransferMsg.Inbound();
            ChOtpGenerateForAccountTransferRequestBean responseBean = new ChOtpGenerateForAccountTransferRequestBean();
            responseBean.setAccountNumber(accountNumber);
            responseBean.setAmount(amount);
            responseBean.setDestinationNumber(destinationNumber);
            responseBean.setChAccountTransferType(ChAccountTransferType.valueOf(chAccountTransferType));
            OtpGenerateForAccountTransferMsg.Outbound outbound = (OtpGenerateForAccountTransferMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, OtpGenerateForAccountTransferMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/achTransfer"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String achTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String secondPassword = jsonObject.get("secondPassword").getAsString();
            String sourceDepositNumber = jsonObject.get("sourceDepositNumber").getAsString();
            String ibanNumber = jsonObject.get("ibanNumber").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            GetIBANsOwnerNameMsg.Inbound ibanInbound = new GetIBANsOwnerNameMsg.Inbound();
            ChIBANsOwnerRequestBean requestBeanIban = new ChIBANsOwnerRequestBean();
            List<String> ibanNumbers = new ArrayList();
            ibanNumbers.add(ibanNumber);
            requestBeanIban.setIbanNumbers(ibanNumbers);
            ibanInbound.setRequestBean(requestBeanIban);
            GetIBANsOwnerNameMsg.Outbound outboundIban = (GetIBANsOwnerNameMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), ibanInbound, GetIBANsOwnerNameMsg.Outbound.class, sessionId);
            ChIBANsOwnerResponseBean responseBeanIban = outboundIban.getResponseBean();
            AchNormalTransferMsg.Inbound inbound = new AchNormalTransferMsg.Inbound();
            ChAchNormalTransferRequestBean requestBean = new ChAchNormalTransferRequestBean();
            requestBean.setAmount(new BigDecimal(amount));
            requestBean.setSecondPassword(secondPassword);
            requestBean.setSourceDepositNumber(sourceDepositNumber);
            requestBean.setIbanNumber(ibanNumber);
            requestBean.setOwnerName(((ChIBANsOwnerResponseBean.IBANOwnerResponseBean)responseBeanIban.getIbanOwnerResponseBeen().stream().filter(Objects::nonNull).findFirst().get()).getOwnerName());
            requestBean.setChSecondPasswordType(ChSecondPasswordType.TRANSFER_SECOND_PASS);
            inbound.setRequestBean(requestBean);
            AchNormalTransferMsg.Outbound outbound = (AchNormalTransferMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, AchNormalTransferMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/rtgsTransfer"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String rtgsTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String secondPassword = jsonObject.get("secondPassword").getAsString();
            String sourceDepositNumber = jsonObject.get("sourceDepositNumber").getAsString();
            String ibanNumber = jsonObject.get("ibanNumber").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            String description = jsonObject.get("description").getAsString();
            String factorNumber = jsonObject.get("reason").getAsString();
            String receiverTelephoneNumber = jsonObject.get("receiverTelephoneNumber").getAsString();
            GetIBANsOwnerNameMsg.Inbound ibanInbound = new GetIBANsOwnerNameMsg.Inbound();
            ChIBANsOwnerRequestBean requestBeanIban = new ChIBANsOwnerRequestBean();
            List<String> ibanNumbers = new ArrayList();
            ibanNumbers.add(ibanNumber);
            requestBeanIban.setIbanNumbers(ibanNumbers);
            ibanInbound.setRequestBean(requestBeanIban);
            GetIBANsOwnerNameMsg.Outbound outboundIban = (GetIBANsOwnerNameMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), ibanInbound, GetIBANsOwnerNameMsg.Outbound.class, sessionId);
            ChIBANsOwnerResponseBean responseBeanIban = outboundIban.getResponseBean();
            RtgsNormalTransferMsg.Inbound inbound = new RtgsNormalTransferMsg.Inbound();
            ChRtgsNormalTransferRequestBean rtgsNormalTransferRequestBean = new ChRtgsNormalTransferRequestBean();
            rtgsNormalTransferRequestBean.setAmount(new BigDecimal(amount));
            rtgsNormalTransferRequestBean.setSecondPassword(secondPassword);
            rtgsNormalTransferRequestBean.setSourceDepositNumber(sourceDepositNumber);
            rtgsNormalTransferRequestBean.setDestinationIbanNumber(ibanNumber);
            rtgsNormalTransferRequestBean.setReceiverName(((ChIBANsOwnerResponseBean.IBANOwnerResponseBean)responseBeanIban.getIbanOwnerResponseBeen().stream().filter(Objects::nonNull).findFirst().get()).getOwnerName());
            rtgsNormalTransferRequestBean.setReceiverFamily(((ChIBANsOwnerResponseBean.IBANOwnerResponseBean)responseBeanIban.getIbanOwnerResponseBeen().stream().filter(Objects::nonNull).findFirst().get()).getOwnerName());
            rtgsNormalTransferRequestBean.setReceiverTelephoneNumber(receiverTelephoneNumber);
            rtgsNormalTransferRequestBean.setChSecondPasswordType(ChSecondPasswordType.TRANSFER_SECOND_PASS);
            rtgsNormalTransferRequestBean.setDescription(description);
            rtgsNormalTransferRequestBean.setFactorNumber(factorNumber);
            inbound.setRtgsNormalTransferRequestBean(rtgsNormalTransferRequestBean);
            RtgsNormalTransferMsg.Outbound outbound = (RtgsNormalTransferMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, RtgsNormalTransferMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/bridgeTransfer"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String bridgeTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String secondPassword = jsonObject.get("secondPassword").getAsString();
            String sourceDepositNumber = jsonObject.get("sourceDepositNumber").getAsString();
            String ibanNumber = jsonObject.get("ibanNumber").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            String description = jsonObject.get("description").getAsString();
            GetIBANsOwnerNameMsg.Inbound ibanInbound = new GetIBANsOwnerNameMsg.Inbound();
            ChIBANsOwnerRequestBean requestBeanIban = new ChIBANsOwnerRequestBean();
            List<String> ibanNumbers = new ArrayList();
            ibanNumbers.add(ibanNumber);
            requestBeanIban.setIbanNumbers(ibanNumbers);
            ibanInbound.setRequestBean(requestBeanIban);
            GetIBANsOwnerNameMsg.Outbound outboundIban = (GetIBANsOwnerNameMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), ibanInbound, GetIBANsOwnerNameMsg.Outbound.class, sessionId);
            ChIBANsOwnerResponseBean responseBeanIban = outboundIban.getResponseBean();
            PostPaymentOrderMsg.Inbound inbound = new PostPaymentOrderMsg.Inbound();
            ChPostPaymentOrderRequestBean requestBean = new ChPostPaymentOrderRequestBean();
            ChPaymentOrderRqBean chPaymentOrderRqBean = new ChPaymentOrderRqBean();
            ChCreditorPartyIdentificationBean creditorIdentification = new ChCreditorPartyIdentificationBean();
            creditorIdentification.setIban(ibanNumber);
            creditorIdentification.setName(((ChIBANsOwnerResponseBean.IBANOwnerResponseBean)responseBeanIban.getIbanOwnerResponseBeen().stream().filter(Objects::nonNull).findFirst().get()).getOwnerName());
            chPaymentOrderRqBean.setCreditorIdentification(creditorIdentification);
            ChDebtorPartyIdentificationBean debtorIdentification = new ChDebtorPartyIdentificationBean();
            debtorIdentification.setAccountNumber(sourceDepositNumber);
            chPaymentOrderRqBean.setDebtorIdentification(debtorIdentification);
            chPaymentOrderRqBean.setAmount(new BigDecimal(amount));
            chPaymentOrderRqBean.setPaymentPurpose(description);
            requestBean.setSecondPassword(secondPassword);
            requestBean.setChSecondPasswordType(ChSecondPasswordType.TRANSFER_SECOND_PASS);
            requestBean.setChPaymentOrderRqBean(chPaymentOrderRqBean);
            inbound.setRequestBean(requestBean);
            PostPaymentOrderMsg.Outbound outbound = provider.execute(Util.getChMessageHeader(request), inbound, PostPaymentOrderMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getStatement"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getStatement(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String deposit = jsonObject.get("deposit").getAsString();
            JsonElement action = jsonObject.get("action");
            JsonElement order = jsonObject.get("order");
            String fromDate = jsonObject.get("fromDate").getAsString();
            String toDate = jsonObject.get("toDate").getAsString();
            long length = jsonObject.get("length").getAsLong();
            long offset = jsonObject.get("offset").getAsLong();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GetStatementMsg.Inbound inbound = new GetStatementMsg.Inbound();
            ChStatementSearchRequestBean chStatementSearchRequestBean = new ChStatementSearchRequestBean();
            chStatementSearchRequestBean.setDepositNumber(deposit);
            chStatementSearchRequestBean.setAction(action != null ? ChActionType.fromValue(action.getAsString()) : null);
            chStatementSearchRequestBean.setChStatementSearchDirection(order != null ? ChStatementSearchDirection.fromValue(order.getAsString()) : null);
            chStatementSearchRequestBean.setFromDate(formatter.parse(fromDate));
            chStatementSearchRequestBean.setToDate(formatter.parse(toDate));
            chStatementSearchRequestBean.setLength(length);
            chStatementSearchRequestBean.setOffset(offset);
            inbound.setChStatementSearchRequestBean(chStatementSearchRequestBean);
            GetStatementMsg.Outbound outbound = (GetStatementMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetStatementMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getTransactionReceipts"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getTransactionReceipts(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String pan = jsonObject.get("pan").getAsString();
            String fromDate = jsonObject.get("fromDate").getAsString();
            String toDate = jsonObject.get("toDate").getAsString();
            long length = jsonObject.get("length").getAsLong();
            long offset = jsonObject.get("offset").getAsLong();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GetCardTransactionsMsg.Inbound inbound = new GetCardTransactionsMsg.Inbound();
            ChCardTransactionsRequestBean requestBean = new ChCardTransactionsRequestBean();
            requestBean.setFromDate(formatter.parse(fromDate));
            requestBean.setToDate(formatter.parse(toDate));
            requestBean.setLength(length);
            requestBean.setOffset(offset);
            List<ChCardTransactionType> types = new ArrayList();
            types.add(ChCardTransactionType.CREDIT);
            types.add(ChCardTransactionType.DEBIT);
            types.add(ChCardTransactionType.ACTIVITY);
            types.add(ChCardTransactionType.OTHER);
            inbound.setRequestBean(requestBean);
            GetCardTransactionsMsg.Outbound outbound = (GetCardTransactionsMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetCardTransactionsMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getCardDeposits"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCardDeposits(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String pan = jsonObject.get("pan").getAsString();
            GetCardDepositsMsg.Inbound inbound = new GetCardDepositsMsg.Inbound();
            ChCardDepositRequestBean requestBean = new ChCardDepositRequestBean();
            requestBean.setPan(pan);
            inbound.setRequestBean(requestBean);
            GetCardDepositsMsg.Outbound outbound = (GetCardDepositsMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetCardDepositsMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            String nationalCodeType = jsonObject.get("nationalCodeType").getAsString();
            NationalCodeType type = NationalCodeType.valueOf(nationalCodeType);
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
            MGCustomerExistenceInquiryMsg.Outbound outbound = (MGCustomerExistenceInquiryMsg.Outbound)this.lotusJmsService.receive(id, MGCustomerExistenceInquiryMsg.Outbound.class);
            responseMessage.setMessage(outbound);
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
            value = {"/getCustomerDefaultMobile"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCustomerDefaultMobile(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            GetCustomerDefaultMobileMsg.Inbound inbound = new GetCustomerDefaultMobileMsg.Inbound();
            GetCustomerDefaultMobileMsg.Outbound outbound = (GetCustomerDefaultMobileMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetCustomerDefaultMobileMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/openDeposit"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String openDeposit(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonElement addressId = jsonObject.get("addressId");
            JsonElement currency = jsonObject.get("currency");
            JsonElement depositBranchCode = jsonObject.get("depositBranchCode");
            JsonElement depositGroup = jsonObject.get("depositGroup");
            JsonElement depositType = jsonObject.get("depositType");
            JsonElement investmentDeposit = jsonObject.get("investmentDeposit");
            JsonElement reportDate = jsonObject.get("reportDate");
            JsonElement reportDurationUnit = jsonObject.get("reportDurationUnit");
            JsonElement sameSignatureDepositNumber = jsonObject.get("sameSignatureDepositNumber");
            JsonElement savingDeposit = jsonObject.get("savingDeposit");
            JsonElement withdrawableAmount = jsonObject.get("withdrawableAmount");
            JsonElement withdrawableDeposit = jsonObject.get("withdrawableDeposit");
            OpenDepositMsg.Inbound inbound = new OpenDepositMsg.Inbound();
            ChOpenDepositRequestBean requestBean = new ChOpenDepositRequestBean();
            requestBean.setAddressId(addressId.getAsString());
            requestBean.setDepositGroup(ChDepositGroupType.fromValue(depositGroup.getAsString()));
            requestBean.setCurrency(currency.getAsString());
            requestBean.setDepositBranchCode(depositBranchCode.getAsLong());
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            requestBean.setReportDate(format.parse(reportDate.getAsString()));
            requestBean.setReportDurationUnit(ChInaugurateDepositDurationUnit.fromValue(reportDurationUnit.getAsString()));
            requestBean.setWithdrawableAmount(withdrawableAmount.getAsBigDecimal());
            requestBean.setWithdrawableDeposit(withdrawableDeposit.getAsString());
            requestBean.setSameSignatureDepositNumber(sameSignatureDepositNumber.getAsString());
            requestBean.setDepositType(depositType.getAsLong());
            if (investmentDeposit != null) {
                JsonObject object = investmentDeposit.getAsJsonObject();
                ChInvestmentDepositBean chInvestmentDepositBean = new ChInvestmentDepositBean();
                chInvestmentDepositBean.setInterestToDepositNumber(object.get("interestToDepositNumber").getAsString());
                chInvestmentDepositBean.setInterestSettlementDay(object.get("interestSettlementDay").getAsShort());
                requestBean.setInvestmentDeposit(chInvestmentDepositBean);
            }

            if (savingDeposit != null) {
                JsonObject object = savingDeposit.getAsJsonObject();
                ChSavingDepositBean chSavingDepositBean = new ChSavingDepositBean();
                chSavingDepositBean.setDuration(object.get("duration").getAsLong());
                chSavingDepositBean.setPaymentDepositNumber(object.get("paymentDepositNumber").getAsString());
                chSavingDepositBean.setLifeCycleMonth(object.get("lifeCycleMonth").getAsLong());
                chSavingDepositBean.setPaymentAmount(object.get("paymentAmount").getAsBigDecimal());
                requestBean.setSavingDeposit(chSavingDepositBean);
            }

            inbound.setOpenDepositRequestBean(requestBean);
            OpenDepositMsg.Outbound outbound = (OpenDepositMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, OpenDepositMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/closeDeposit"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String closeDeposit(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            String destinationDeposit = jsonObject.get("destinationDeposit").getAsString();
            String description = jsonObject.get("description").getAsString();
            CloseAccountMsg.Inbound inbound = new CloseAccountMsg.Inbound();
            ChCloseAccountRequestBean requestBean = new ChCloseAccountRequestBean();
            requestBean.setDepositNumber(depositNumber);
            requestBean.setDescription(description);
            requestBean.setDestinationDeposit(destinationDeposit);
            inbound.setRequestBean(requestBean);
            CloseAccountMsg.Outbound outbound = (CloseAccountMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, CloseAccountMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/hotCard"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String hotCard(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String pan = jsonObject.get("pan").getAsString();
            String reason = jsonObject.get("reason").getAsString();
            HotCardMsg.Inbound inbound = new HotCardMsg.Inbound();
            ChPanRequestBean panRequestBean = new ChPanRequestBean();
            panRequestBean.setPan(pan);
            panRequestBean.setReason(reason);
            inbound.setPanRequestBean(panRequestBean);
            HotCardMsg.Outbound outbound = (HotCardMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, HotCardMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/activeWarmCard"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String activeWarmCard(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String cardNumber = jsonObject.get("cardNumber").getAsString();
            String cardPassword = jsonObject.get("cardPassword").getAsString();
            ActiveWarmCardByCardNumberMsg.Inbound inbound = new ActiveWarmCardByCardNumberMsg.Inbound();
            ChActiveWarmCardByCardNumberRequestBean requestBean = new ChActiveWarmCardByCardNumberRequestBean();
            requestBean.setCardNumber(cardNumber);
            requestBean.setCardPassword(cardPassword);
            inbound.setRequestBean(requestBean);
            ActiveWarmCardByCardNumberMsg.Outbound outbound = (ActiveWarmCardByCardNumberMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, ActiveWarmCardByCardNumberMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/printCardInfo"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String printCardInfoMsg(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String cardNumber = jsonObject.get("cardNumber").getAsString();
            PrintCardInfoMsg.Inbound inbound = new PrintCardInfoMsg.Inbound();
            ChPrintCardInfoRequestBean requestBean = new ChPrintCardInfoRequestBean();
            requestBean.setCardNumber(cardNumber);
            inbound.setRequestBean(requestBean);
            PrintCardInfoMsg.Outbound outbound = (PrintCardInfoMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, PrintCardInfoMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/cardOtpRegistration"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String cardOtpRegistration(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String cardNumber = jsonObject.get("cardNumber").getAsString();
            String otpKey = jsonObject.get("otpKey").getAsString();
            String otpPin = jsonObject.get("otpPin").getAsString();
            boolean hasSmsOtp = jsonObject.get("hasSmsOtp").getAsBoolean();
            CardOtpRegistrationMsg.Inbound inbound = new CardOtpRegistrationMsg.Inbound();
            ChCardOtpRegistrationRequestBean requestBean = new ChCardOtpRegistrationRequestBean();
            requestBean.setCardNumber(cardNumber);
            requestBean.setOtpKey(otpKey);
            requestBean.setOtpPin(otpPin);
            requestBean.setHasSmsOtp(hasSmsOtp);
            inbound.setRequestBean(requestBean);
            CardOtpRegistrationMsg.Outbound outbound = (CardOtpRegistrationMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, CardOtpRegistrationMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/activateCardOtp"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String activateCardOtp(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String cardNumber = jsonObject.get("cardNumber").getAsString();
            String otpKey = jsonObject.get("otpKey").getAsString();
            ActivateCardOtpMsg.Inbound inbound = new ActivateCardOtpMsg.Inbound();
            ChCardOtpRequestBean requestBean = new ChCardOtpRequestBean();
            requestBean.setCardNumber(cardNumber);
            requestBean.setOtpKey(otpKey);
            inbound.setRequestBean(requestBean);
            PrintCardInfoMsg.Outbound outbound = (PrintCardInfoMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, PrintCardInfoMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/hotCardList"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String hotCardList(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            LoadWarmCardsByCustomerIdMsg.Inbound inbound = new LoadWarmCardsByCustomerIdMsg.Inbound();
            ChLoadWarmCardsByCustomerIdRequestBean requestBean = new ChLoadWarmCardsByCustomerIdRequestBean();
            inbound.setRequestBean(requestBean);
            LoadWarmCardsByCustomerIdMsg.Outbound outbound = (LoadWarmCardsByCustomerIdMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, LoadWarmCardsByCustomerIdMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getChequeBook"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getChequeBook(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            GetChequeBookListMsg.Inbound inbound = new GetChequeBookListMsg.Inbound();
            ChBaseSearchChequeBookRequestBean requestBean = new ChBaseSearchChequeBookRequestBean();
            requestBean.setDepositNumber(depositNumber);
            inbound.setBaseSearchChequeBookRequestBean(requestBean);
            GetChequeBookListMsg.Outbound outbound = (GetChequeBookListMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetChequeBookListMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/getCheque"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String getCheque(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String chequeBookNumber = jsonObject.get("chequeBookNumber").getAsString();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            GetChequeMsg.Inbound inbound = new GetChequeMsg.Inbound();
            ChChequeSearchRequestBean chequeSearchRequestBean = new ChChequeSearchRequestBean();
            chequeSearchRequestBean.setChequeBookNumber(chequeBookNumber);
            chequeSearchRequestBean.setDepositNumber(depositNumber);
            chequeSearchRequestBean.setChMediaType(ChChequeMediaType.Normal);
            inbound.setChequeSearchRequestBean(chequeSearchRequestBean);
            GetChequeMsg.Outbound outbound = (GetChequeMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetChequeMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/blockCheque"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String blockCheque(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            JsonArray chequeNumbers = jsonObject.get("chequeBookNumber").getAsJsonArray();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            String blockedReason = jsonObject.get("blockedReason").getAsString();
            BlockChequeMsg.Inbound inbound = new BlockChequeMsg.Inbound();
            ChBlockChequeRequestBean chequeRequestBean = new ChBlockChequeRequestBean();
            Type listType = (new TypeToken<List<String>>() {
            }).getType();
            List<String> chequeNumberList = (List)this.gson.fromJson(chequeNumbers, listType);
            chequeRequestBean.setChequeNumbers(chequeNumberList);
            chequeRequestBean.setDepositNumber(depositNumber);
            chequeRequestBean.setBlockedReason(ChBlockedChequeReasonType.fromValue(blockedReason));
            inbound.setChequeRequestBean(chequeRequestBean);
            GetChequeMsg.Outbound outbound = (GetChequeMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, GetChequeMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/blockDeposit"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String BlockDeposit(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            BigDecimal amount = jsonObject.get("amount").getAsBigDecimal();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            String dueDate = jsonObject.get("dueDate").getAsString();
            String narrative = jsonObject.get("narrative").getAsString();
            BlockDepositAmountMsg.Inbound inbound = new BlockDepositAmountMsg.Inbound();
            ChCoreBlockDepositAmountRequestBean requestBean = new ChCoreBlockDepositAmountRequestBean();
            requestBean.setAmount(amount);
            requestBean.setDueDate((new SimpleDateFormat("yyyy/mm/dd")).parse(dueDate));
            requestBean.setIbanOrAccountNo(depositNumber);
            requestBean.setNarrative(narrative);
            requestBean.setBlockReason(ChBlockReason.TRADE_FINANCE);
            requestBean.setSubBlockType(ChSubBlockType.TRADE_FINANCE);
            requestBean.setEbTrackId(UUID.randomUUID().toString());
            inbound.setRequestBean(requestBean);
            BlockDepositAmountMsg.Outbound outbound = (BlockDepositAmountMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, BlockDepositAmountMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/chequeRegister"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    @ResponseBody
    public String chequeRegister(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = "";
            String sayadId = jsonObject.get("sayadId").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            String date = jsonObject.get("date").getAsString();
            String description = jsonObject.get("description").getAsString();
            JsonElement reason = jsonObject.get("reason");
            JsonElement toIban = jsonObject.get("toIban");
            String chequeType = jsonObject.get("chequeType").getAsString();
            String ibanEnquiryResponse = this.ibanEnquiry(message, request);
            PichakError pichakError = new PichakError(ibanEnquiryResponse);
            if (pichakError != null && !pichakError.getCode().equals("0")) {
                throw new Exception(pichakError.getMessage());
            } else {
                JsonObject ibanObject = (JsonObject)this.gson.fromJson(ibanEnquiryResponse, JsonObject.class);
                JsonElement messageElement = ibanObject.get("message");
                JsonElement messageObjectElement = (JsonElement)this.gson.fromJson(messageElement.getAsString(), JsonElement.class);
                JsonObject messageObject = (JsonObject)this.gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                JsonObject responseBean = messageObject.get("mgAccountInfoDto").getAsJsonObject();
                if (responseBean.get("accountNumber") == null) {
                    throw new PichakException("errorCode01:IBAN_MISTAKE", "IBAN_MISTAKE");
                } else {
                    String accountNumber = responseBean.get("accountNumber").getAsString();
                    JsonArray receivers = jsonObject.get("receivers").getAsJsonArray();
                    List<ChPichakPersonInfoDto> chPichakPersonInfoBeanList = new ArrayList();

                    for(JsonElement object : receivers) {
                        JsonObject receiver = object.getAsJsonObject();
                        String name = receiver.get("name").getAsString();
                        String customerID = receiver.get("customerID").getAsString();
                        ChPichakPersonInfoDto chPichakPersonInfoBean = new ChPichakPersonInfoDto();
                        if (receiver.get("shahabId") != null) {
                            String shahabId = receiver.get("shahabId").getAsString();
                            chPichakPersonInfoBean.setShahabId(shahabId);
                        }

                        String nationalCodeType = receiver.get("nationalCodeType").getAsString();
                        NationalCodeType type = NationalCodeType.valueOf(nationalCodeType);
                        switch (type) {
                            case INDIVIDUAL:
                                chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.IR_INDIVIDUAL);
                                break;
                            case CORPORATE:
                                chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.IR_CORPORATE);
                                break;
                            case INDIVIDUAL_FOREIGNER:
                                chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.NON_IR_INDIVIDUAL);
                                break;
                            case CORPORATE_FOREIGNER:
                                chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.NON_IR_CORPORATE);
                        }

                        chPichakPersonInfoBean.setName(name);
                        chPichakPersonInfoBean.setCustomerID(customerID);
                        chPichakPersonInfoBeanList.add(chPichakPersonInfoBean);
                    }

                    MGPichakIssueChequeMsg.Inbound inbound = new MGPichakIssueChequeMsg.Inbound();
                    ChPichakIssueChequeReqDto issueChequeReqDto = new ChPichakIssueChequeReqDto();
                    issueChequeReqDto.setReceivers(chPichakPersonInfoBeanList);
                    issueChequeReqDto.setSayadId(sayadId);
                    if (toIban != null) {
                        issueChequeReqDto.setToIban(toIban.getAsString());
                    }

                    issueChequeReqDto.setAmount(new BigDecimal(amount));
                    issueChequeReqDto.setDueDate(DateUtil.convertDateToSqlDate(this.utility.getDateTime(date, Locale.forLanguageTag("fa"), "yyyy/mm/dd")));
                    issueChequeReqDto.setDescription(description);
                    if (reason != null) {
                        issueChequeReqDto.setReason(ChPichakReasonType.valueOf(reason.getAsString()));
                    }

                    issueChequeReqDto.setChequeType(ChPichakChequeType.valueOf(chequeType));
                    Map map = new HashMap();
                    map.put("sessionId", sessionId);
                    map.put("accountNo", accountNumber);
                    map.put("amount", amount);
                    map.put("date", date);
                    String conditionResponse = this.getWithdrawConditions(this.makeResponse(map), request);
                    JsonObject conditionObject = (JsonObject)this.gson.fromJson(conditionResponse, JsonObject.class);
                    messageElement = conditionObject.get("message");
                    messageObjectElement = (JsonElement)this.gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    messageObject = (JsonObject)this.gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                    JsonArray withdrawConditionsArray = messageObject.get("withDrawalConditionDtos").getAsJsonArray();
                    Long conditionCode = -1L;
                    Iterator responseConditionCustomers = withdrawConditionsArray.iterator();
                    if (responseConditionCustomers.hasNext()) {
                        JsonElement object = (JsonElement)responseConditionCustomers.next();
                        conditionCode = object.getAsJsonObject().get("id").getAsLong();
                    }

                    issueChequeReqDto.setCreditConditionId(conditionCode);
                    map.put("conditionId", conditionCode);
                    String responseConditionCustomer = this.getWithdrawalConditionCustomers(makeResponse(map), request);
                    JsonObject conditionCustomersObject = gson.fromJson(responseConditionCustomer, JsonObject.class);
                    messageElement = conditionCustomersObject.get("message");
                    messageObjectElement = (JsonElement)this.gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    messageObject = (JsonObject)this.gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                    JsonArray conditionCustomerArray = messageObject.get("withDrawalConditionCustomerDtos").getAsJsonArray();
                    List<WithDrawalConditionCustomerDto> chWithDrawalConditionCustomerBeanList = new ArrayList();
                    WithDrawalConditionCustomerDto chWithDrawalConditionCustomerBean = new WithDrawalConditionCustomerDto();

                    for(JsonElement object : conditionCustomerArray) {
                        Long clientId = object.getAsJsonObject().get("clientId").getAsLong();
                        Integer place = object.getAsJsonObject().get("place").getAsInt();
                        String clientTitle = object.getAsJsonObject().get("clientTitle").getAsString();
                        Boolean isStamp = object.getAsJsonObject().get("isStamp").getAsBoolean();
                        chWithDrawalConditionCustomerBean.setClientId(clientId);
                        chWithDrawalConditionCustomerBean.setClientTitle(clientTitle);
                        chWithDrawalConditionCustomerBean.setPlace(place);
                        chWithDrawalConditionCustomerBean.setStamp(isStamp);
                        chWithDrawalConditionCustomerBean.setConditionId(conditionCode);
                        chWithDrawalConditionCustomerBeanList.add(chWithDrawalConditionCustomerBean);
                    }

                    issueChequeReqDto.setSigners(chWithDrawalConditionCustomerBeanList);
                    inbound.setIssueChequeReqDto(issueChequeReqDto);
                    Gson gsonBuilder = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss.S").create();
                    String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGPichakIssueChequeMsg", RequestType.INQUIRY, gsonBuilder.toJson(inbound));
                    MGPichakIssueChequeMsg.Outbound outbound = (MGPichakIssueChequeMsg.Outbound)this.lotusJmsService.receive(id, MGPichakIssueChequeMsg.Outbound.class);
                    ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
                    if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                        throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
                    } else {
                        responseMessage.setMessage(outbound);
                    }
                }
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

    @RequestMapping(
            path = {"/chequeTransfer"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    @ResponseBody
    public String chequeTransfer(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sayadId = jsonObject.get("sayadId").getAsString();
            JsonElement toIban = jsonObject.get("toIban");
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            String clientType = jsonObject.get("nationalCodeType").getAsString();
            boolean accept = jsonObject.get("acceptTransfer").getAsBoolean();
            JsonElement giveBack = jsonObject.get("giveBack");
            JsonElement reason = jsonObject.get("reason");
            String description = jsonObject.get("description").getAsString();
            ChPichakTransferChequeDto requestBean = new ChPichakTransferChequeDto();
            String responseInquiry = this.chequeInquiry(message, request);
            JsonObject responseInquiryObj = (JsonObject)this.gson.fromJson(responseInquiry, JsonObject.class);
            JsonElement errorInquiry = responseInquiryObj.get("error");
            if (errorInquiry != null && !errorInquiry.getAsJsonObject().get("code").getAsString().equals("0")) {
                JsonObject errorInquiryObj = errorInquiry.getAsJsonObject();
                throw new PichakException(errorInquiryObj.get("message").getAsString(), errorInquiryObj.get("code").getAsString());
            } else {
                ChPichakInquiryChequeRespDto chPichakInquiryChequeRespDto = (ChPichakInquiryChequeRespDto)this.gson.fromJson(responseInquiryObj.get("message").getAsString(), ChPichakInquiryChequeRespDto.class);
                String ibanEnquiryResponse = this.ibanEnquiry(message, request);
                PichakError pichakError = new PichakError(ibanEnquiryResponse);
                if (pichakError != null && !pichakError.getCode().equals("0")) {
                    throw new Exception(pichakError.getMessage());
                } else {
                    JsonObject ibanObject = (JsonObject)this.gson.fromJson(ibanEnquiryResponse, JsonObject.class);
                    JsonElement messageElement = ibanObject.get("message");
                    JsonElement messageObjectElement = (JsonElement)this.gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    JsonObject messageObject = (JsonObject)this.gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                    JsonObject accountInfoBean = messageObject.get("mgAccountInfoDto").getAsJsonObject();
                    if (accountInfoBean.get("accountNumber") == null) {
                        throw new ChannelManagerException("errorCode01:IBAN_MISTAKE");
                    } else {
                        String accountNumber = accountInfoBean.get("accountNumber").getAsString();
                        requestBean.setSayadId(sayadId);
                        requestBean.setAccountExternalRef(accountNumber);
                        requestBean.setDescription(description);
                        requestBean.setAcceptTransfer(accept);
                        if (giveBack != null && giveBack.getAsBoolean()) {
                            requestBean.setToIban((String)null);
                            requestBean.setReason((ChPichakReasonType)null);
                            requestBean.setReceivers((List)null);
                            requestBean.setGiveBack(ChPichakGiveBackType.GIVE_BACK);
                        } else {
                            if (reason != null) {
                                requestBean.setReason(ChPichakReasonType.valueOf(reason.getAsString()));
                            }

                            if (toIban != null) {
                                requestBean.setToIban(toIban.getAsString());
                            }

                            requestBean.setGiveBack(ChPichakGiveBackType.NONE);
                            JsonArray receivers = jsonObject.get("receivers").getAsJsonArray();
                            List<ChPichakPersonInfoDto> chPichakPersonInfoBeanList = new ArrayList();

                            for(JsonElement object : receivers) {
                                JsonObject receiver = object.getAsJsonObject();
                                String name = receiver.get("name").getAsString();
                                String customerID = receiver.get("customerID").getAsString();
                                ChPichakPersonInfoDto chPichakPersonInfoBean = new ChPichakPersonInfoDto();
                                if (receiver.get("shahabId") != null && receiver.get("shahabId").getAsInt() != 0) {
                                    String shahabId = receiver.get("shahabId").getAsString();
                                    chPichakPersonInfoBean.setShahabId(shahabId);
                                }

                                chPichakPersonInfoBean.setName(name);
                                chPichakPersonInfoBean.setCustomerID(customerID);
                                chPichakPersonInfoBeanList.add(chPichakPersonInfoBean);
                                String nationalCodeType = receiver.get("nationalCodeType").getAsString();
                                NationalCodeType type = NationalCodeType.valueOf(nationalCodeType);
                                switch (type) {
                                    case INDIVIDUAL:
                                        chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.IR_INDIVIDUAL);
                                        break;
                                    case CORPORATE:
                                        chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.IR_CORPORATE);
                                        break;
                                    case INDIVIDUAL_FOREIGNER:
                                        chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.NON_IR_INDIVIDUAL);
                                        break;
                                    case CORPORATE_FOREIGNER:
                                        chPichakPersonInfoBean.setCustomerType(ChPichakCustomerType.NON_IR_CORPORATE);
                                }
                            }

                            requestBean.setReceivers(chPichakPersonInfoBeanList);
                        }

                        String customerInfos = this.getCustomerInfo(message, request);
                        JsonObject customerInfosObj = (JsonObject)this.gson.fromJson(customerInfos, JsonObject.class);
                        JsonObject messageCustomerInfosObj = (JsonObject)this.gson.fromJson(customerInfosObj.get("message").getAsString(), JsonObject.class);
                        JsonObject responseDto = messageCustomerInfosObj.get("responseDto").getAsJsonObject();
                        long customerId = responseDto.get("customerNo").getAsLong();
                        String shahabCode = responseDto.get("shahabCode").getAsString();
                        requestBean.setHolderClientId(customerId);
                        String name = "";
                        if (responseDto.get("name") != null) {
                            name = name.concat(responseDto.get("name").getAsString());
                        }

                        if (responseDto.get("family") != null) {
                            name = name.concat(" ");
                            name = name.concat(responseDto.get("family").getAsString());
                        }

                        ClientType type = ClientType.valueOf(clientType);
                        ChPichakPersonInfoDto pichakPersonInfoBean = new ChPichakPersonInfoDto();
                        pichakPersonInfoBean.setName(name);
                        pichakPersonInfoBean.setShahabId(shahabCode);
                        pichakPersonInfoBean.setCustomerID(nationalCode);
                        switch (type) {
                            case INDIVIDUAL:
                                pichakPersonInfoBean.setCustomerType(ChPichakCustomerType.IR_INDIVIDUAL);
                                break;
                            case CORPORATE:
                                pichakPersonInfoBean.setCustomerType(ChPichakCustomerType.IR_CORPORATE);
                                break;
                            case INDIVIDUAL_FOREIGNER:
                                pichakPersonInfoBean.setCustomerType(ChPichakCustomerType.NON_IR_INDIVIDUAL);
                                break;
                            case CORPORATE_FOREIGNER:
                                pichakPersonInfoBean.setCustomerType(ChPichakCustomerType.NON_IR_CORPORATE);
                        }

                        requestBean.setHolderClient(pichakPersonInfoBean);
                        Map map = new HashMap();
                        map.put("accountNo", accountNumber);
                        map.put("amount", chPichakInquiryChequeRespDto.getAmount());
                        map.put("date", this.utility.getDateTime(new Date(), new Locale("fa")));
                        String conditionResponse = this.getWithdrawConditions(this.makeResponse(map), request);
                        JsonObject conditionObject = (JsonObject)this.gson.fromJson(conditionResponse, JsonObject.class);
                        messageElement = conditionObject.get("message");
                        JsonElement msgElement = (JsonElement)this.gson.fromJson(messageElement.getAsString(), JsonElement.class);
                        messageObject = (JsonObject)this.gson.fromJson(msgElement.getAsString(), JsonObject.class);
                        JsonArray withdrawConditionsArray = messageObject.get("withDrawalConditionDtos").getAsJsonArray();
                        Long conditionCode = -1L;
                        Iterator var43 = withdrawConditionsArray.iterator();
                        if (var43.hasNext()) {
                            JsonElement object = (JsonElement)var43.next();
                            conditionCode = object.getAsJsonObject().get("id").getAsLong();
                        }

                        requestBean.setCreditConditionId(conditionCode);
                        map.put("conditionId", conditionCode);
                        String responseConditionCustomers = this.getWithdrawalConditionCustomers(this.makeResponse(map), request);
                        JsonObject conditionCustomersObject = (JsonObject)this.gson.fromJson(responseConditionCustomers, JsonObject.class);
                        messageElement = conditionCustomersObject.get("message");
                        JsonElement conditionElement = (JsonElement)this.gson.fromJson(messageElement.getAsString(), JsonElement.class);
                        messageObject = (JsonObject)this.gson.fromJson(conditionElement.getAsString(), JsonObject.class);
                        JsonArray conditionCustomerArray = messageObject.get("withDrawalConditionCustomerDtos").getAsJsonArray();
                        List<WithDrawalConditionCustomerDto> signers = new ArrayList();
                        WithDrawalConditionCustomerDto chWithDrawalConditionCustomerBean = new WithDrawalConditionCustomerDto();

                        for(JsonElement object : conditionCustomerArray) {
                            Long clientId = object.getAsJsonObject().get("clientId").getAsLong();
                            Integer place = object.getAsJsonObject().get("place").getAsInt();
                            String clientTitle = object.getAsJsonObject().get("clientTitle").getAsString();
                            Integer signNeedNo = object.getAsJsonObject().get("signNeedNo").getAsInt();
                            Boolean isStamp = object.getAsJsonObject().get("isStamp").getAsBoolean();
                            chWithDrawalConditionCustomerBean.setClientId(clientId);
                            chWithDrawalConditionCustomerBean.setClientTitle(clientTitle);
                            chWithDrawalConditionCustomerBean.setPlace(place);
                            chWithDrawalConditionCustomerBean.setStamp(isStamp);
                            chWithDrawalConditionCustomerBean.setSignNeedNo(signNeedNo);
                            chWithDrawalConditionCustomerBean.setConditionId(conditionCode);
                            signers.add(chWithDrawalConditionCustomerBean);
                        }

                        requestBean.setSigners(signers);
                        MGPichakTransferChequeMsg.Inbound inbound = new MGPichakTransferChequeMsg.Inbound();
                        inbound.setTransferChequeDto(requestBean);
                        String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGPichakTransferChequeMsg", RequestType.INQUIRY, inbound);
                        MGPichakTransferChequeMsg.Outbound outbound = (MGPichakTransferChequeMsg.Outbound)this.lotusJmsService.receive(id, MGPichakTransferChequeMsg.Outbound.class);
                        ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
                        if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                            throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
                        } else {
                            responseMessage.setMessage(outbound);
                        }
                    }
                }
            }
        } catch (PichakException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage());
        } catch (CoreException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getMessage());
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError("127", e.getMessage());
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            path = {"/getWithdrawConditions"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getWithdrawConditions(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String accountNo = jsonObject.get("accountNo").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            String date = jsonObject.get("date").getAsString();
            MGGetWithDrawalConditionsMsg.Inbound inbound = new MGGetWithDrawalConditionsMsg.Inbound();
            inbound.setAccountNumber(accountNo);
            inbound.setDate((new Utility()).getDateTime(date, new Locale("fa")));
            inbound.setAmount(new BigDecimal(amount));
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGGetWithDrawalConditionsMsg", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
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
            path = {"/getWithdrawalConditionCustomers"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getWithdrawalConditionCustomers(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String accountNo = jsonObject.get("accountNo").getAsString();
            Long conditionId = jsonObject.get("conditionId").getAsLong();
            MGGetWithDrawalConditionCustomersMsg.Inbound inbound = new MGGetWithDrawalConditionCustomersMsg.Inbound();
            inbound.setExternalRef(accountNo);
            inbound.setWithDrawalConditionId(conditionId);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGGetWithDrawalConditionCustomersMsg", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
        } catch (Exception e) {
            error = new PichakError(e);
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }

    @RequestMapping(
            path = {"/ibanEnquiry"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String ibanEnquiry(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String iban = jsonObject.get("iban").getAsString();
            MGIbanInquiryServiceMsg.Inbound inbound = new MGIbanInquiryServiceMsg.Inbound();
            inbound.setIban(iban);
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGIbanInquiryServiceMsg", RequestType.INQUIRY, inbound);
            String outbound = this.lotusJmsService.receive(id);
            responseMessage.setMessage(outbound);
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
            path = {"/getIban"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getIban(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String depositNumber = jsonObject.get("depositNumber").getAsString();
            ConvertDepositNumberToIbanMsg.Inbound inbound = new ConvertDepositNumberToIbanMsg.Inbound();
            ChDepositNumberToIbanRequestBean requestBean = new ChDepositNumberToIbanRequestBean();
            requestBean.setDepositNumber(depositNumber);
            inbound.setRequestBean(requestBean);
            ConvertDepositNumberToIbanMsg.Outbound outbound = (ConvertDepositNumberToIbanMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, ConvertDepositNumberToIbanMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            path = {"/chequeInquiry"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    public String chequeInquiryWithPersianDate(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sayadId = jsonObject.get("sayadId").getAsString();
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            AAAServer.logger.info("****chequeInquiry****");
            MGPichakInquiryChequeMsg.Inbound inbound = new MGPichakInquiryChequeMsg.Inbound();
            ChPichakPersonInfoDto client = new ChPichakPersonInfoDto();
            if (jsonObject.get("shahabId") != null) {
                String shahabId = jsonObject.get("shahabId").getAsString();
                client.setShahabId(shahabId);
            } else {
                String customerInfos = this.getCustomerInfo(message, request);
                JsonObject customerInfosObj = (JsonObject)this.gson.fromJson(customerInfos, JsonObject.class);
                JsonObject messageCustomerInfosObj = (JsonObject)this.gson.fromJson(customerInfosObj.get("message").getAsString(), JsonObject.class);
                JsonObject responseDto = messageCustomerInfosObj.get("responseDto").getAsJsonObject();
                long customerId = responseDto.get("customerNo").getAsLong();
                AAAServer.logger.info("customerId: " + customerId);
                inbound.setClientId(customerId);
            }

            AAAServer.logger.info("sayadId: " + sayadId);
            AAAServer.logger.info("nationalCode: " + nationalCode);
            AAAServer.logger.info("shahabId: " + client.getShahabId());
            client.setCustomerID(nationalCode);
            inbound.setClient(client);
            inbound.setSayadId(sayadId);
            AAAServer.logger.info("inbound: " + this.gson.toJson(inbound));
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGPichakInquiryChequeMsg", RequestType.INQUIRY, inbound);
            MGPichakInquiryChequeMsg.Outbound outbound = (MGPichakInquiryChequeMsg.Outbound)this.lotusJmsService.receive(id, MGPichakInquiryChequeMsg.Outbound.class);
            ChPichakInquiryChequeRespDto responseDto = outbound.getResponseDto();
            AAAServer.logger.info("outbound:" + this.gson.toJson(outbound));
            ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
            if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
            } else {
                String date = this.utility.getPersianDate(responseDto.getDueDate());
                JsonObject response = (JsonObject)this.gson.fromJson(this.gson.toJson(responseDto), JsonObject.class);
                response.addProperty("dueDate", date);
                responseMessage.setMessage(response);
            }
        } catch (PichakException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage());
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
            path = {"/chequeInquiryInternal"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    public String chequeInquiry(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sayadId = jsonObject.get("sayadId").getAsString();
            String nationalCode = jsonObject.get("nationalCode").getAsString();
            AAAServer.logger.info("****chequeInquiry****");
            MGPichakInquiryChequeMsg.Inbound inbound = new MGPichakInquiryChequeMsg.Inbound();
            ChPichakPersonInfoDto client = new ChPichakPersonInfoDto();
            if (jsonObject.get("shahabId") != null) {
                String shahabId = jsonObject.get("shahabId").getAsString();
                client.setShahabId(shahabId);
            } else {
                String customerInfos = this.getCustomerInfo(message, request);
                JsonObject customerInfosObj = (JsonObject)this.gson.fromJson(customerInfos, JsonObject.class);
                JsonObject messageCustomerInfosObj = (JsonObject)this.gson.fromJson(customerInfosObj.get("message").getAsString(), JsonObject.class);
                JsonObject responseDto = messageCustomerInfosObj.get("responseDto").getAsJsonObject();
                long customerId = responseDto.get("customerNo").getAsLong();
                AAAServer.logger.info("customerId: " + customerId);
                inbound.setClientId(customerId);
            }

            AAAServer.logger.info("sayadId: " + sayadId);
            AAAServer.logger.info("nationalCode: " + nationalCode);
            AAAServer.logger.info("shahabId: " + client.getShahabId());
            client.setCustomerID(nationalCode);
            inbound.setClient(client);
            inbound.setSayadId(sayadId);
            AAAServer.logger.info("inbound: " + this.gson.toJson(inbound));
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGPichakInquiryChequeMsg", RequestType.INQUIRY, inbound);
            MGPichakInquiryChequeMsg.Outbound outbound = (MGPichakInquiryChequeMsg.Outbound)this.lotusJmsService.receive(id, MGPichakInquiryChequeMsg.Outbound.class);
            ChPichakInquiryChequeRespDto responseDto = outbound.getResponseDto();
            AAAServer.logger.info("outbound:" + this.gson.toJson(outbound));
            ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
            if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
            } else {
                responseMessage.setMessage(responseDto);
            }
        } catch (PichakException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage());
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
            path = {"/chequeAccept"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String chequeAccept(@RequestBody String message, HttpServletRequest request) throws ParseException {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sayadId = jsonObject.get("sayadId").getAsString();
            boolean accept = jsonObject.get("accept").getAsBoolean();
            String name = jsonObject.get("name").getAsString();
            String description = jsonObject.get("description").getAsString();
            String clientType = jsonObject.get("nationalCodeType").getAsString();
            ClientType type = ClientType.valueOf(clientType);
            ChPichakPersonInfoDto acceptorClient = new ChPichakPersonInfoDto();
            if (jsonObject.get("shahabCode") != null) {
                String shahabId = jsonObject.get("shahabCode").getAsString();
                acceptorClient.setShahabId(shahabId);
            } else {
                String customerInfos = this.getCustomerInfo(message, request);
                JsonObject customerInfosObj = (JsonObject)this.gson.fromJson(customerInfos, JsonObject.class);
                JsonObject messageCustomerInfosObj = (JsonObject)this.gson.fromJson(customerInfosObj.get("message").getAsString(), JsonObject.class);
                JsonObject responseDto = messageCustomerInfosObj.get("responseDto").getAsJsonObject();
                String shahabCode = responseDto.get("shahabCode").getAsString();
                acceptorClient.setShahabId(shahabCode);
            }

            String customerID = jsonObject.get("nationalCode").getAsString();
            MGPichakAcceptChequeMsg.Inbound inbound = new MGPichakAcceptChequeMsg.Inbound();
            ChPichakAcceptChequeDto requestBean = new ChPichakAcceptChequeDto();
            requestBean.setSayadId(sayadId);
            requestBean.setAcceptDate(new java.sql.Date(System.currentTimeMillis()));
            requestBean.setAccept(accept);
            requestBean.setAcceptDescription(description);
            acceptorClient.setName(name);
            acceptorClient.setCustomerID(customerID);
            switch (type) {
                case INDIVIDUAL:
                    acceptorClient.setCustomerType(ChPichakCustomerType.IR_INDIVIDUAL);
                    break;
                case CORPORATE:
                    acceptorClient.setCustomerType(ChPichakCustomerType.IR_CORPORATE);
                    break;
                case INDIVIDUAL_FOREIGNER:
                    acceptorClient.setCustomerType(ChPichakCustomerType.NON_IR_INDIVIDUAL);
                    break;
                case CORPORATE_FOREIGNER:
                    acceptorClient.setCustomerType(ChPichakCustomerType.NON_IR_CORPORATE);
            }

            requestBean.setAcceptorClient(acceptorClient);
            inbound.setAcceptChequeDto(requestBean);
            Gson gsonBuilder = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss.S").create();
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "com.caspian.banking.ebanking.message.deposit.pichak.MGPichakAcceptChequeMsg", RequestType.INQUIRY, gsonBuilder.toJson(inbound));
            MGPichakAcceptChequeMsg.Outbound outbound = (MGPichakAcceptChequeMsg.Outbound)this.lotusJmsService.receive(id, MGPichakAcceptChequeMsg.Outbound.class);
            ChPichakExceptionDto exceptionBean = outbound.getError();
            if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
            } else {
                responseMessage.setMessage(outbound);
            }
        } catch (PichakException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage());
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
    public String getCustomerInfoByPan(@RequestBody String message, HttpServletRequest request) throws DatatypeConfigurationException, ParseException {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String pan = jsonObject.get("pan").getAsString();
            LoadCardCustomerInfoByNumberMsg.Inbound inbound = new LoadCardCustomerInfoByNumberMsg.Inbound();
            ChLoadCardCustomerInfoByNumberRequestBean requestBean = new ChLoadCardCustomerInfoByNumberRequestBean();
            requestBean.setCardNumber(pan);
            inbound.setRequestBean(requestBean);
            LoadCardCustomerInfoByNumberMsg.Outbound outbound = (LoadCardCustomerInfoByNumberMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, LoadCardCustomerInfoByNumberMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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

    @RequestMapping(
            value = {"/carExchangeAssignCustomerAccount"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String carExchangeAssignCustomerAccount(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String accountNumber = jsonObject.get("accountNumber").getAsString();
            String bourseCode = jsonObject.get("bourseCode").getAsString();
            CarExchangeAssignCustomerAccountMsg.Inbound inbound = new CarExchangeAssignCustomerAccountMsg.Inbound();
            ChCarExchangeAssignCustomerAccountRequestBean requestBean = new ChCarExchangeAssignCustomerAccountRequestBean();
            requestBean.setAccountNumber(accountNumber);
            requestBean.setBourseCode(bourseCode);
            inbound.setRequestBean(requestBean);
            CarExchangeAssignCustomerAccountMsg.Outbound outbound = (CarExchangeAssignCustomerAccountMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, CarExchangeAssignCustomerAccountMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
        } catch (InvalidSessionException e) {
            UserResource.sessionId = null;
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), this.errorCodeNull, this.errorCodeLatin);
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

    @RequestMapping(
            value = {"/carExchangeActivateAssignCustomerAccount"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String carExchangeActivateAssignCustomerAccount(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String accountNumber = jsonObject.get("accountNumber").getAsString();
            String bourseCode = jsonObject.get("bourseCode").getAsString();
            CarExchangeActivateAssignCustomerAccountMsg.Inbound inbound = new CarExchangeActivateAssignCustomerAccountMsg.Inbound();
            ChCarExchangeActivateAssignCustomerAccountRequestBean requestBean = new ChCarExchangeActivateAssignCustomerAccountRequestBean();
            requestBean.setAccountNumber(accountNumber);
            requestBean.setBourseCode(bourseCode);
            inbound.setRequestBean(requestBean);
            CarExchangeActivateAssignCustomerAccountMsg.Outbound outbound = (CarExchangeActivateAssignCustomerAccountMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, CarExchangeActivateAssignCustomerAccountMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
        } catch (InvalidSessionException e) {
            UserResource.sessionId = null;
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), this.errorCodeNull, this.errorCodeLatin);
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

    @RequestMapping(
            value = {"/carExchangeDeactivateAssignCustomerAccount"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String carExchangeDeactivateAssignCustomerAccount(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String accountNumber = jsonObject.get("accountNumber").getAsString();
            String bourseCode = jsonObject.get("bourseCode").getAsString();
            CarExchangeDeactivateAssignCustomerAccountMsg.Inbound inbound = new CarExchangeDeactivateAssignCustomerAccountMsg.Inbound();
            ChCarExchangeDeactivateAssignCustomerAccountRequestBean requestBean = new ChCarExchangeDeactivateAssignCustomerAccountRequestBean();
            requestBean.setAccountNumber(accountNumber);
            requestBean.setBourseCode(bourseCode);
            inbound.setRequestBean(requestBean);
            CarExchangeDeactivateAssignCustomerAccountMsg.Outbound outbound = (CarExchangeDeactivateAssignCustomerAccountMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, CarExchangeDeactivateAssignCustomerAccountMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
        } catch (InvalidSessionException e) {
            UserResource.sessionId = null;
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), this.errorCodeNull, this.errorCodeLatin);
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

    @RequestMapping(
            value = {"/carExchangeGetAssignedCustomerAccounts"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String carExchangeGetAssignedCustomerAccounts(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            CarExchangeGetAssignedCustomerAccountsMsg.Inbound inbound = new CarExchangeGetAssignedCustomerAccountsMsg.Inbound();
            CarExchangeGetAssignedCustomerAccountsMsg.Outbound outbound = (CarExchangeGetAssignedCustomerAccountsMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, CarExchangeGetAssignedCustomerAccountsMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
        } catch (InvalidSessionException e) {
            UserResource.sessionId = null;
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getErrorCode(), e.getMessage(), this.errorCodeNull, this.errorCodeLatin);
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

    @RequestMapping(
            value = {"/loadGeneralParameter"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String loadGeneralParameter(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String code = jsonObject.get("code").getAsString();
            MGLoadGeneralParameterMsg.Inbound inbound = new MGLoadGeneralParameterMsg.Inbound();
            inbound.setMgParentCode(code);
            inbound.setActive(true);
            Gson gsonBuilder = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss.S").create();
            String id = this.lotusJmsService.send(this.coreUsername, this.coreBranchcode, "channelManagement.MGLoadCharityAccountsMsg", RequestType.INQUIRY, gsonBuilder.toJson(inbound));
            String outbound = this.lotusJmsService.receive(id);
            JsonElement element = (JsonElement)this.gson.fromJson(outbound, JsonElement.class);
            JsonObject object = element.getAsJsonObject();
            JsonArray mgGeneralParameterDTO = object.get("mgGeneralParameterDTO").getAsJsonArray();
            JsonObject response = new JsonObject();
            JsonObject responseBean = new JsonObject();
            responseBean.add("loadGeneralParameterBeanList", mgGeneralParameterDTO);
            response.add("responseBean", responseBean);
            responseMessage.setMessage(response);
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
            value = {"/translateException"},
            produces = {"application/json"},
            consumes = {"application/json"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public String translateException(@RequestBody String message, HttpServletRequest request) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(message, JsonObject.class);
            String sessionId = jsonObject.get("sessionId").getAsString();
            String channel = jsonObject.get("channel").getAsString();
            String exceptionType = jsonObject.get("exceptionType").getAsString();
            TranslateExceptionMsg.Inbound inbound = new TranslateExceptionMsg.Inbound();
            ChExceptionTranslationRequestBean requestBean = new ChExceptionTranslationRequestBean();
            requestBean.setChannel(channel);
            requestBean.setExceptionType(exceptionType);
            List<ChExceptionTranslationRequestBean.Locale> localeList = new ArrayList();
            localeList.add(com.caspian.moderngateway.core.coreservice.dto.ChExceptionTranslationRequestBean.Locale.fa_IR);
            requestBean.setLocaleList(localeList);
            inbound.setRequestBean(requestBean);
            TranslateExceptionMsg.Outbound outbound = (TranslateExceptionMsg.Outbound)this.provider.execute(Util.getChMessageHeader(request), inbound, TranslateExceptionMsg.Outbound.class, sessionId);
            responseMessage.setMessage(outbound);
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
}
