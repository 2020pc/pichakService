package com.caspian.pichak.ResourceServer;

import com.caspian.banking.deposit.cheque.sayad.dto.ChequeInquiryResponseDTO;
import com.caspian.banking.deposit.cheque.sayad.message.ChequeInquiryBySayadIdMsg;
import com.caspian.banking.ebanking.dto.*;
import com.caspian.banking.ebanking.dto.deposit.pichak.*;
import com.caspian.banking.ebanking.message.MGGetWithDrawalConditionCustomersMsg;
import com.caspian.banking.ebanking.message.MGGetWithDrawalConditionsMsg;
import com.caspian.banking.ebanking.message.card.MGLoadCardCustomerInfoByNumberMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakAcceptChequeMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakInquiryChequeMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakIssueChequeMsg;
import com.caspian.banking.ebanking.message.deposit.pichak.MGPichakTransferChequeMsg;
import com.caspian.banking.message.RequestType;
import com.caspian.banking.model.dto.ChCustomerExistenceInquiryRequestDto;
import com.caspian.banking.model.dto.ChCustomerExistenceInquiryResponseDto;
import com.caspian.banking.model.messages.MGCustomerExistenceInquiryMsg;
import com.caspian.banking.model.messages.MGIbanInquiryServiceMsg;
import com.caspian.banking.util.DateUtil;
import com.caspian.moderngateway.core.channelmanagerinfrastructure.exception.ChannelManagerException;
import com.caspian.moderngateway.core.coreservice.dto.ChBaseSearchChequeBookRequestBean;
import com.caspian.moderngateway.core.coreservice.dto.ChChangePasswordRequestBean;
import com.caspian.moderngateway.core.coreservice.dto.ChChequeSearchRequestBean;
import com.caspian.moderngateway.core.message.ChangePasswordMsg;
import com.caspian.moderngateway.core.message.GetChequeBookListMsg;
import com.caspian.moderngateway.core.message.GetChequeMsg;
import com.caspian.pichak.exceptions.CoreException;
import com.caspian.pichak.exceptions.PichakException;
import com.caspian.pichak.model.dto.ISOMessageDTO;
import com.caspian.pichak.model.dto.PichakError;
import com.caspian.pichak.model.dto.ResponseMessage;
import com.caspian.pichak.type.ClientType;
import com.caspian.pichak.type.NationalCodeType;
import com.caspian.pichak.type.RejectCauseType;
import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.utility.Util;
import com.google.gson.*;
import com.pb.ouc.util.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.*;


@RestController
@RequestMapping({"/digital/atm"})
public class ATMResource extends BaseResource {
    private Utility utility = new Utility();
    private Util util = new Util();



    public String chequeRegister(@RequestBody ISOMessageDTO message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {

            String sessionId = "";
            String sayadId = message.getBody().get("sayadId").getAsString();
            String amount = message.getBody().get("amount").getAsString();
            String date = message.getDate();
            String description = "register from ATM";
            String reason = message.getBody().get("reason").getAsString();
            JsonElement toIban =message.getBody().get("toIban");
            int chequeType = message.getBody().get("chequeType").getAsInt();
            String ibanEnquiryResponse = ibanEnquiry(message.getBody().get("iban").getAsString());
            PichakError pichakError = new PichakError(ibanEnquiryResponse);
            if (pichakError != null && !pichakError.getCode().equals("0")) {
                throw new Exception(pichakError.getMessage());
            } else {
                JsonObject ibanObject = gson.fromJson(ibanEnquiryResponse, JsonObject.class);
                JsonElement messageElement = ibanObject.get("message");
                JsonElement messageObjectElement = gson.fromJson(messageElement.getAsString(), JsonElement.class);
                JsonObject messageObject = gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                JsonObject responseBean = messageObject.get("mgAccountInfoDto").getAsJsonObject();
                if (responseBean.get("accountNumber") == null) {
                    throw new PichakException("errorCode01:IBAN_MISTAKE", "IBAN_MISTAKE");
                } else {
                    String accountNumber = responseBean.get("accountNumber").getAsString();
                    JsonArray receivers = message.getBody().get("receivers").getAsJsonArray();
                    List<ChPichakPersonInfoDto> chPichakPersonInfoBeanList = new ArrayList();

                    for (JsonElement object : receivers) {
                        JsonObject receiver = object.getAsJsonObject();
                        String name = receiver.get("name").getAsString();
                        String customerID = receiver.get("customerID").getAsString();
                        ChPichakPersonInfoDto chPichakPersonInfoBean = new ChPichakPersonInfoDto();
                        if (receiver.get("shahabId") != null && receiver.get("shahabId").getAsString().trim().length() > 0 && receiver.get("shahabId").getAsInt() > 0) {
                            String shahabId = receiver.get("shahabId").getAsString();
                            chPichakPersonInfoBean.setShahabId(shahabId);
                        }

                        String nationalCodeType = receiver.get("nationalCodeType").getAsString();
                        NationalCodeType type = NationalCodeType.fromValue(nationalCodeType);
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
                    issueChequeReqDto.setDueDate(DateUtil.convertDateToSqlDate(utility.getDateTime(date, Locale.forLanguageTag("fa"), "yyyy/mm/dd")));
                    issueChequeReqDto.setDescription(description);
                    if (reason.trim().length() > 0) {
                        issueChequeReqDto.setReason(ChPichakReasonType.valueOf(reason));
                    }

                    issueChequeReqDto.setChequeType(ChPichakChequeType.fromValue(chequeType));
                    Map map = new HashMap();
                    map.put("sessionId", sessionId);
                    map.put("accountNo", accountNumber);
                    map.put("amount", amount);
                    map.put("date", date);
                    String conditionResponse = getWithdrawConditions(makeResponse(map));
                    JsonObject conditionObject = gson.fromJson(conditionResponse, JsonObject.class);
                    messageElement = conditionObject.get("message");
                    messageObjectElement = gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    messageObject = gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                    JsonArray withdrawConditionsArray = messageObject.get("withDrawalConditionDtos").getAsJsonArray();
                    Long conditionCode = -1L;
                    Iterator responseConditionCustomers = withdrawConditionsArray.iterator();
                    if (responseConditionCustomers.hasNext()) {
                        JsonElement object = (JsonElement) responseConditionCustomers.next();
                        conditionCode = object.getAsJsonObject().get("id").getAsLong();
                    }

                    issueChequeReqDto.setCreditConditionId(conditionCode);
                    map.put("conditionId", conditionCode);
                    String responseConditionCustomersValue = getWithdrawalConditionCustomers(makeResponse(map));
                    JsonObject conditionCustomersObject = gson.fromJson(responseConditionCustomersValue, JsonObject.class);
                    messageElement = conditionCustomersObject.get("message");
                    messageObjectElement = gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    messageObject = gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                    JsonArray conditionCustomerArray = messageObject.get("withDrawalConditionCustomerDtos").getAsJsonArray();
                    List<WithDrawalConditionCustomerDto> chWithDrawalConditionCustomerBeanList = new ArrayList();
                    WithDrawalConditionCustomerDto chWithDrawalConditionCustomerBean = new WithDrawalConditionCustomerDto();

                    for (JsonElement object : conditionCustomerArray) {
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
                    String id = lotusJmsService.send(coreUsername, coreBranchcode, "channelManagement.MGPichakIssueChequeMsg", RequestType.INQUIRY, gsonBuilder.toJson(inbound));
                    MGPichakIssueChequeMsg.Outbound outbound =  lotusJmsService.receive(id, MGPichakIssueChequeMsg.Outbound.class);
                    ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
                    if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                        throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
                    } else {
                        byte[] response = new byte[1];
                        int index = 0;
                        this.utility.setIsoFormat("0".getBytes(), 1, index, response);
                        responseMessage.setMessage(this.utility.bytesToHex(response));
                        Object var73 = null;
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


    public String chequeTransfer(ISOMessageDTO message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {

            String sayadId = message.getBody().get("sayadId").getAsString();
            JsonElement toIban = message.getBody().get("toIban");
            String nationalCode = message.getUser().getNationalCode();
            long customerId = Long.parseLong(message.getUser().getCustomerId());
            int accept = message.getBody().get("accept").getAsInt();
            JsonElement giveBack = message.getBody().get("giveBack");
            JsonElement reason = message.getBody().get("reason");
            String description = "transfer from ATM";
            ChPichakTransferChequeDto requestBean = new ChPichakTransferChequeDto();
            ChPichakInquiryChequeRespDto chPichakInquiryChequeRespDto = chequeInquiry(sayadId, nationalCode, customerId);
//            JsonObject ibanRequest = new JsonObject();
//            ibanRequest.addProperty("iban", chPichakInquiryChequeRespDto.getFromIban());
            String ibanEnquiryResponse = ibanEnquiry(chPichakInquiryChequeRespDto.getFromIban());
            PichakError pichakError = new PichakError(ibanEnquiryResponse);
            if (pichakError != null && !pichakError.getCode().equals("0")) {
                throw new Exception(pichakError.getMessage());
            } else {
                JsonObject ibanObject = gson.fromJson(ibanEnquiryResponse, JsonObject.class);
                JsonElement messageElement = ibanObject.get("message");
                JsonElement messageObjectElement = gson.fromJson(messageElement.getAsString(), JsonElement.class);
                JsonObject messageObject = gson.fromJson(messageObjectElement.getAsString(), JsonObject.class);
                JsonObject accountInfoBean = messageObject.get("mgAccountInfoDto").getAsJsonObject();
                if (accountInfoBean.get("accountNumber") == null) {
                    throw new ChannelManagerException("errorCode01:IBAN_MISTAKE");
                } else {
                    String accountNumber = accountInfoBean.get("accountNumber").getAsString();
                    requestBean.setSayadId(sayadId);
                    requestBean.setAccountExternalRef(accountNumber);
                    requestBean.setDescription(description);
                    requestBean.setAcceptTransfer(accept == 1);
                    if (giveBack != null && giveBack.getAsInt() == 1) {
                        requestBean.setToIban(null);
                        requestBean.setReason(null);
                        requestBean.setReceivers(null);
                        requestBean.setGiveBack(ChPichakGiveBackType.GIVE_BACK);
                    } else {
                        if (reason != null && reason.getAsString().trim().length() > 0) {
                            requestBean.setReason(ChPichakReasonType.valueOf(reason.getAsString()));
                        }

                        if (toIban != null) {
                            requestBean.setToIban(toIban.getAsString());
                        }

                        JsonArray receivers = message.getBody().get("receivers").getAsJsonArray();
                        List<ChPichakPersonInfoDto> chPichakPersonInfoBeanList = new ArrayList();

                        for (JsonElement object : receivers) {
                            JsonObject receiver = object.getAsJsonObject();
                            String name = receiver.get("name").getAsString();
                            String customerID = receiver.get("customerID").getAsString();
                            ChPichakPersonInfoDto chPichakPersonInfoBean = new ChPichakPersonInfoDto();
                            if (receiver.get("shahabId") != null && receiver.get("shahabId").getAsString().trim().length() > 0 && receiver.get("shahabId").getAsInt() > 0) {
                                String shahabId = receiver.get("shahabId").getAsString();
                                chPichakPersonInfoBean.setShahabId(shahabId);
                            }

                            chPichakPersonInfoBean.setName(name);
                            chPichakPersonInfoBean.setCustomerID(customerID);
                            chPichakPersonInfoBeanList.add(chPichakPersonInfoBean);
                            String nationalCodeType = receiver.get("nationalCodeType").getAsString();
                            NationalCodeType type = NationalCodeType.fromValue(nationalCodeType);
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

                    String name = message.getBody().get("name").getAsString();
                    String clientType = message.getBody().get("clientType").getAsString();
                    ClientType type = ClientType.fromValue(clientType);
                    ChPichakPersonInfoDto pichakPersonInfoBean = new ChPichakPersonInfoDto();
                    pichakPersonInfoBean.setName(name);
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
                    map.put("date", utility.getDateTime(new Date(), new Locale("fa")));
                    String conditionResponse = getWithdrawConditions(makeResponse(map));
                    JsonObject conditionObject = gson.fromJson(conditionResponse, JsonObject.class);
                    messageElement = conditionObject.get("message");
                    JsonElement msgElement = gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    messageObject = gson.fromJson(msgElement.getAsString(), JsonObject.class);
                    JsonArray withdrawConditionsArray = messageObject.get("withDrawalConditionDtos").getAsJsonArray();
                    Long conditionCode = -1L;
                    Iterator var37 = withdrawConditionsArray.iterator();
                    if (var37.hasNext()) {
                        JsonElement object = (JsonElement) var37.next();
                        conditionCode = object.getAsJsonObject().get("id").getAsLong();
                    }

                    requestBean.setCreditConditionId(conditionCode);
                    map.put("conditionId", conditionCode);
                    String responseConditionCustomers = getWithdrawalConditionCustomers(makeResponse(map));
                    JsonObject conditionCustomersObject = gson.fromJson(responseConditionCustomers, JsonObject.class);
                    messageElement = conditionCustomersObject.get("message");
                    JsonElement conditionElement = gson.fromJson(messageElement.getAsString(), JsonElement.class);
                    messageObject = gson.fromJson(conditionElement.getAsString(), JsonObject.class);
                    JsonArray conditionCustomerArray = messageObject.get("withDrawalConditionCustomerDtos").getAsJsonArray();
                    List<WithDrawalConditionCustomerDto> signers = new ArrayList();
                    WithDrawalConditionCustomerDto chWithDrawalConditionCustomerBean = new WithDrawalConditionCustomerDto();

                    for (JsonElement object : conditionCustomerArray) {
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
                    String id = lotusJmsService.send(coreUsername, coreBranchcode, "channelManagement.MGPichakTransferChequeMsg", RequestType.INQUIRY, inbound);
                    MGPichakTransferChequeMsg.Outbound outbound = lotusJmsService.receive(id, MGPichakTransferChequeMsg.Outbound.class);
                    ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
                    if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                        throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
                    } else {
                        byte[] response = new byte[1];
                        int index = 0;
                        utility.setIsoFormat("0".getBytes(), 1, index, response);
                        responseMessage.setMessage(this.utility.bytesToHex(response));
                        Object var80 = null;
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


    public String getWithdrawConditions(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            String accountNo = jsonObject.get("accountNo").getAsString();
            String amount = jsonObject.get("amount").getAsString();
            String date = jsonObject.get("date").getAsString();
            MGGetWithDrawalConditionsMsg.Inbound inbound = new MGGetWithDrawalConditionsMsg.Inbound();
            inbound.setAccountNumber(accountNo);
            inbound.setDate((new Utility()).getDateTime(date, new Locale("fa")));
            inbound.setAmount(new BigDecimal(amount));
            String id = lotusJmsService.send(coreUsername, coreBranchcode, "channelManagement.MGGetWithDrawalConditionsMsg", RequestType.INQUIRY, inbound);
            String outbound = lotusJmsService.receive(id);
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


    public String getWithdrawalConditionCustomers(@RequestBody String message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
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


    public String ibanEnquiry( String iban) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {

            MGIbanInquiryServiceMsg.Inbound inbound = new MGIbanInquiryServiceMsg.Inbound();
            inbound.setIban(iban);
            String id = lotusJmsService.send(coreUsername, coreBranchcode, "channelManagement.MGIbanInquiryServiceMsg", RequestType.INQUIRY, inbound);
            String outbound = lotusJmsService.receive(id);
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


    public String chequeInquiry( ISOMessageDTO message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            String sayadId = message.getUser().getShahabCode();
            String nationalCode = message.getUser().getNationalCode();
            String customerId = message.getUser().getCustomerId();
            ChPichakPersonInfoDto client = new ChPichakPersonInfoDto();
            client.setShahabId(message.getUser().getShahabCode());

            AAAServer.logger.info("****chequeInquiry****");
            AAAServer.logger.info("sayadId: " + sayadId);
            AAAServer.logger.info("nationalCode: " + nationalCode);
            AAAServer.logger.info("customerId: " + customerId);
            AAAServer.logger.info("shahabId: " + client.getShahabId());
            MGPichakInquiryChequeMsg.Inbound inbound = new MGPichakInquiryChequeMsg.Inbound();
            client.setCustomerID(message.getUser().getCustomerId());
            inbound.setClient(client);
            inbound.setClientId(Long.valueOf(customerId));
            inbound.setSayadId(sayadId);
            AAAServer.logger.info("inbound: " + gson.toJson(inbound));

            MGPichakInquiryChequeMsg.Outbound outbound = lotusJmsService.sendInquiry(inbound);

            ChPichakInquiryChequeRespDto responseDto = outbound.getResponseDto();
            AAAServer.logger.info("outbound:" + gson.toJson(outbound));
            ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
            if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
            } else {
                ChPichakChequeStatus pichakChequeStatus = responseDto.getChequeStatus();
                ChPichakBlockStatus pichakBlockStatus = responseDto.getBlockStatus();
                ChPichakGuaranteeStatus pichakGuaranteeStatus = responseDto.getGuaranteeStatus();
                ChPichakChequeType chPichakChequeType = responseDto.getChequeType();
                ChPichakChequeMediaType chequeMedia = responseDto.getChequeMedia();
                List<ChPichakPersonInfoDto> chPichakPersonInfoBeanList = responseDto.getChequeOwners();
                List<Map> pichakPersonInfoList = new ArrayList();

                for (ChPichakPersonInfoDto chPichakPersonInfoBean : chPichakPersonInfoBeanList) {
                    Map map = new HashMap();
                    map.put("name", chPichakPersonInfoBean.getName());
                    map.put("customerID", chPichakPersonInfoBean.getCustomerID());
                    map.put("shahabId", chPichakPersonInfoBean.getShahabId());
                    map.put("pichakCustomerType", chPichakPersonInfoBean.getCustomerType().value());
                    pichakPersonInfoList.add(map);
                }

                byte[] response = new byte[256];
                int index = 0;
                index = utility.setIsoFormat((new BigInteger(utility.getPersianDigitDate(responseDto.getDueDate()))).toByteArray(), 4, index, response);
                index = utility.setIsoFormat(responseDto.getAmount().toBigInteger().toByteArray(), 8, index, response);
                index = utility.setIsoFormat((new BigInteger(responseDto.getBankCode())).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(responseDto.getBranchCode())).toByteArray(), 4, index, response);
                index = utility.setIsoFormat(utility.hexToBytes(responseDto.getSayadId()), 8, index, response);
                index = utility.setIsoFormat((new BigInteger(responseDto.getSerialNo())).toByteArray(), 4, index, response);
                index = utility.setIsoFormat(utility.hexToBytes(responseDto.getFromIban().replaceAll("IR", "").replaceAll("ir", "")), 12, index, response);
                String locked = responseDto.getLocked() ? "1" : "0";
                index = utility.setIsoFormat((new BigInteger(locked)).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(pichakChequeStatus.value().toString())).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(pichakBlockStatus.value())).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(pichakGuaranteeStatus.value())).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(String.valueOf(chPichakChequeType.value()))).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(chequeMedia.value())).toByteArray(), 1, index, response);
                index = utility.setIsoFormat((new BigInteger(String.valueOf(pichakPersonInfoList.size()))).toByteArray(), 1, index, response);

                for (Map o : pichakPersonInfoList) {
                    index = utility.setIsoFormat(o.get("name").toString().getBytes(StandardCharsets.UTF_8), index, response);
                    index = utility.setIsoFormat(o.get("customerID").toString().getBytes(), index, response);
                    String shahabId = o.get("shahabId") != null ? o.get("shahabId").toString() : "0000000000000000";
                    index = utility.setIsoFormat(utility.hexToBytes(shahabId), 8, index, response);
                    index = utility.setIsoFormat((new BigInteger(String.valueOf(o.get("pichakCustomerType")))).toByteArray(), 1, index, response);
                }

                response = Arrays.copyOf(response, index);
                responseMessage.setMessage(utility.bytesToHex(response));
                Object var40 = null;
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

    public ChPichakInquiryChequeRespDto chequeInquiry(String sayadId, String nationalCode, Long customerId) {
        ChPichakInquiryChequeRespDto responseDto = null;

        try {
            ChPichakPersonInfoDto client = new ChPichakPersonInfoDto();
            AAAServer.logger.info("****chequeInquiry****");
            AAAServer.logger.info("sayadId: " + sayadId);
            AAAServer.logger.info("nationalCode: " + nationalCode);
            AAAServer.logger.info("customerId: " + customerId);
            AAAServer.logger.info("shahabId: " + client.getShahabId());
            MGPichakInquiryChequeMsg.Inbound inbound = new MGPichakInquiryChequeMsg.Inbound();
            client.setCustomerID(nationalCode);
            inbound.setClient(client);
            inbound.setClientId(customerId);
            inbound.setSayadId(sayadId);
            AAAServer.logger.info("inbound: " + gson.toJson(inbound));
            String id = lotusJmsService.send(coreUsername, coreBranchcode, "channelManagement.MGPichakInquiryChequeMsg", RequestType.INQUIRY, inbound);
            MGPichakInquiryChequeMsg.Outbound outbound = lotusJmsService.receive(id, MGPichakInquiryChequeMsg.Outbound.class);
            responseDto = outbound.getResponseDto();
            AAAServer.logger.info("outbound:" + gson.toJson(outbound));
            ChPichakExceptionDto exceptionBean = outbound.getExceptionDto();
            if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
            }
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            throw new Exception(e);
        } finally {
            AAAServer.logger.info(this.gson.toJson(responseDto));
            return responseDto;
        }
    }


    public String chequeAccept(ISOMessageDTO message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            String sayadId = message.getBody().get("sayadId").getAsString();
            String accept = message.getBody().get("accept").getAsString();
            String name = message.getBody().get("name").getAsString();
            String clientType = message.getBody().get("clientType").getAsString();
            ClientType type = ClientType.fromValue(clientType);
            RejectCauseType rejectCauseType = RejectCauseType.fromValue(accept);
            ChPichakPersonInfoDto acceptorClient = new ChPichakPersonInfoDto();
            String shahabId = message.getBody().get("shahabCode").getAsString();
            acceptorClient.setShahabId(shahabId);
            String customerID = message.getBody().get("nationalCode").getAsString();
            MGPichakAcceptChequeMsg.Inbound inbound = new MGPichakAcceptChequeMsg.Inbound();
            ChPichakAcceptChequeDto requestBean = new ChPichakAcceptChequeDto();
            requestBean.setSayadId(sayadId);
            requestBean.setAcceptDate(new java.sql.Date(System.currentTimeMillis()));
            requestBean.setAccept(rejectCauseType.equals(RejectCauseType.ACCEPT));
            requestBean.setAcceptDescription(util.getRejectCause(rejectCauseType));
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
            String id = lotusJmsService.send(coreUsername, coreBranchcode, "com.caspian.banking.ebanking.message.deposit.pichak.MGPichakAcceptChequeMsg", RequestType.INQUIRY, gsonBuilder.toJson(inbound));
            MGPichakAcceptChequeMsg.Outbound outbound = lotusJmsService.receive(id, MGPichakAcceptChequeMsg.Outbound.class);
            ChPichakExceptionDto exceptionBean = outbound.getError();
            if (exceptionBean != null && exceptionBean.getErrorCode() != null) {
                throw new PichakException(exceptionBean.getErrorDescription(), exceptionBean.getErrorCode());
            } else {
                byte[] response = new byte[1];
                int index = 0;
                this.utility.setIsoFormat("0".getBytes(), 1, index, response);
                responseMessage.setMessage(this.utility.bytesToHex(response));
                Object var32 = null;
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


    public String getCustomerInfo(ISOMessageDTO  message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            String nationalCode = message.getUser().getNationalCode();
            String nationalCodeType =message.getBody().get("customerNationalType").getAsString();
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
            MGCustomerExistenceInquiryMsg.Outbound outbound = lotusJmsService.sendInquiry(inbound);
            ChCustomerExistenceInquiryResponseDto responseDto = outbound.getResponseDto();
            byte[] response = new byte[256];
            int index = 0;
            String customerName = responseDto.getName();
            customerName = customerName.concat(" ");
            customerName = customerName.concat(responseDto.getFamily());
            if (customerName.length() > 100) {
                customerName = customerName.substring(0, 100);
            }

            index = this.utility.setIsoFormat(customerName.getBytes(StandardCharsets.UTF_8), index, response);
            response = Arrays.copyOf(response, index);
            responseMessage.setMessage(this.utility.bytesToHex(response));
            Object var25 = null;
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

    public MGLoadCardCustomerInfoByNumberMsg.Outbound getCustomerInfoByPanCore(@RequestBody String pan) throws Exception {


        try {
            MGLoadCardCustomerInfoByNumberMsg.Inbound inbound = new MGLoadCardCustomerInfoByNumberMsg.Inbound();
            inbound.setCardNumber(pan);
//            inbound.setCardNumber("6221061023186077");
            inbound.setValidateCvv2AndExpireDate(false);
            MGLoadCardCustomerInfoByNumberMsg.Outbound res = lotusJmsService.sendInquiry(inbound);
            return res;

        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            throw new Exception(e);
        }
    }

    public String getChequeAndDebtInquiry(ISOMessageDTO message) {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            String sayadId = message.getBody().get("sayadId").getAsString();
            ChequeInquiryBySayadIdMsg.Inbound inbound = new ChequeInquiryBySayadIdMsg.Inbound();
            inbound.setChequeSerial(sayadId);
            String id = lotusJmsService.send(coreUsername, coreBranchcode, "deposit.cheque.sayad.ChequeInquiryBySayadIdMsg", RequestType.INQUIRY, inbound);
            ChequeInquiryBySayadIdMsg.Outbound outbound = lotusJmsService.receive(id, ChequeInquiryBySayadIdMsg.Outbound.class);
            ChequeInquiryResponseDTO responseDto = outbound.getResponseDTO();
            byte[] response = new byte[128];
            int index = 0;
            index = utility.setIsoFormat((new BigInteger(responseDto.getBranchCode())).toByteArray(), 4, index, response);
            index = utility.setIsoFormat((new BigInteger(responseDto.getSeriesNo())).toByteArray(), 4, index, response);
            index = utility.setIsoFormat((new BigInteger(responseDto.getSerialNo())).toByteArray(), 4, index, response);
            index = utility.setIsoFormat((new BigInteger("1")).toByteArray(), 1, index, response);
            index = utility.setIsoFormat((new BigInteger(responseDto.getExpireDate())).toByteArray(), 4, index, response);
            index = utility.setIsoFormat((new BigInteger("0604").toByteArray()), 4, index, response);
            index = utility.setIsoFormat(this.utility.hexToBytes(responseDto.getIban().replaceAll("IR", "").replaceAll("ir", "")), 12, index, response);
            response = Arrays.copyOf(response, index);
            responseMessage.setMessage(this.utility.bytesToHex(response));
            Object var23 = null;
        } catch (CoreException e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError(e.getMessage());
        } catch (NoClassDefFoundError noCF) {
            AAAServer.logger.error("stack trace", noCF.getCause());
            error = new PichakError("127", noCF.getCause().toString());
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
            error = new PichakError("127", e.getMessage());
        } finally {
            responseMessage.setError(error);
            AAAServer.logger.info(responseMessage.toString());
            return responseMessage.toString();
        }
    }
}
