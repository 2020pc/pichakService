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
import com.caspian.banking.message.RequestType;
import com.caspian.banking.model.dto.ChCustomerExistenceInquiryRequestDto;
import com.caspian.banking.model.messages.MGCustomerExistenceInquiryMsg;
import com.caspian.pichak.exceptions.CoreException;
import com.caspian.pichak.model.dto.PichakError;
import com.caspian.pichak.model.dto.ResponseMessage;
import com.caspian.pichak.service.lotus.LotusJmsService;
import com.caspian.pichak.type.NationalCodeType;
import com.caspian.pichak.utility.AAAServer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pb.ouc.util.util.Utility;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping({"/OTP/public"})
public class PublicResource extends BaseResource {
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
            path = {"/getCustomerInformation"},
            method = {RequestMethod.POST},
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public String getCustomerInformation(@RequestBody String message) {
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
    public MGLoadCardCustomerInfoByNumberMsg.Outbound getCustomerInfoByPanCore(@RequestBody String pan) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        PichakError error = new PichakError();

        try {
            MGLoadCardCustomerInfoByNumberMsg.Inbound inbound = new MGLoadCardCustomerInfoByNumberMsg.Inbound();
            inbound.setCardNumber(pan);
//            inbound.setCardNumber("6221061023186077");
            inbound.setValidateCvv2AndExpireDate(false);
            MGLoadCardCustomerInfoByNumberMsg.Outbound res = lotusJmsService.sendInquiry(inbound);
            return res;
//            MGLoadCardCustomerInfoByNumberMsg.Outbound res =  lotusJmsService.receive(id);
//            JsonObject responseObj = gson.fromJson(outbound, JsonObject.class);
//            JsonObject requestMsg = new JsonObject();
//            JsonObject cardCustomerInfoResponseDto = responseObj.get("cardCustomerInfoResponseDto").getAsJsonObject();
//            requestMsg.add("customerNumber", cardCustomerInfoResponseDto.get("customerId"));
//            String customerInformation = this.getCustomerInformation(gson.toJson(requestMsg));
//            JsonObject customerInformationObj = gson.fromJson(customerInformation, JsonObject.class);
//            JsonElement customerInformationMessage = gson.fromJson(customerInformationObj.get("message").getAsString(), JsonElement.class);
//            JsonObject messageObj = gson.fromJson(customerInformationMessage.getAsString(), JsonObject.class);
//            JsonObject customerDTO = messageObj.get("customerDTO").getAsJsonObject();
//            responseObj.add("shahabCode", customerDTO.get("shahabCode"));
//            responseMessage.setMessage(responseObj);
        } catch (Exception e) {
            AAAServer.logger.error("stack trace", e);
           throw new Exception(e);
        } finally {
//            responseMessage.setError(error);
//            AAAServer.logger.info(responseMessage.toString());
//            return responseMessage.toString();
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

}
