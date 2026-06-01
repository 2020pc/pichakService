package com.caspian.pichak.utility;


import com.caspian.moderngateway.infrastructurespi.common.message.ChMessageHeader;
import com.caspian.moderngateway.core.coreservice.dto.pichak.ChPichakBlockStatus;
import com.caspian.moderngateway.core.coreservice.dto.pichak.ChPichakChequeMediaType;
import com.caspian.moderngateway.core.coreservice.dto.pichak.ChPichakChequeStatus;
import com.caspian.moderngateway.core.coreservice.dto.pichak.ChPichakChequeType;
import com.caspian.moderngateway.core.coreservice.dto.pichak.ChPichakCustomerType;
import com.caspian.moderngateway.core.coreservice.dto.pichak.ChPichakGuaranteeStatus;
import com.caspian.moderngateway.infrastructurespi.common.message.ChMessageHeader;
import com.caspian.moderngateway.infrastructurespi.common.message.ChMessageHeader.ChannelServiceType;
import com.caspian.moderngateway.infrastructurespi.common.message.ChMessageHeader.HeaderContext;
import com.caspian.moderngateway.infrastructurespi.common.message.ChMessageHeader.UserAgent;
import com.caspian.pichak.exceptions.InvalidHeaderException;
import com.caspian.pichak.type.RejectCauseType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public final class Util {
    static final String HEADER_STRING = "Authorization";

    public static JsonNode findValueOfJsonField(final String body, final String jwtAppFiledName) throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(body);
        return findNode(jwtAppFiledName, rootNode);
    }

    private static JsonNode findNode(final String jwtAppFiledName, final JsonNode rootNode) throws IOException {
        JsonNode find = rootNode.get(jwtAppFiledName);
        if (find != null) {
            return find;
        } else {
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.properties().iterator();
            if (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> node = (Map.Entry)fieldsIterator.next();
                return findNode(jwtAppFiledName, (JsonNode)node.getValue());
            } else {
                return null;
            }
        }
    }

//    public static JsonNode getClientIdFromJwtHeader(final String jwtAuthorizationHeader, final String jwtAppFiledName) throws Throwable {
//        String[] jwtSplited = jwtAuthorizationHeader.split("\\.");
//        if (jwtSplited.length < 2) {
//            throw new com.caspian.pichak.exceptions.InvalidHeaderException("invalid.jwt.authorization.header");
//        } else {
//            JsonNode appName = findValueOfJsonField(new String(Base64.getDecoder().decode(jwtSplited[1].getBytes()), StandardCharsets.UTF_8), jwtAppFiledName);
//            if (appName != null && !"".equals(appName.asText())) {
//                return appName;
//            } else {
//                throw new com.caspian.pichak.exceptions.InvalidHeaderException("invalid.jwt." + jwtAppFiledName + ".header");
//            }
//        }
//    }

//    public static ChMessageHeader getChMessageHeader() {
//        HashMap<ChMessageHeader.HeaderContext, Object> header = new HashMap();
//        Map<ChMessageHeader.UserAgent, String> userAgentInfo = new HashMap();
//        header.put(HeaderContext.USER_AGENT, userAgentInfo);
//        return new ChMessageHeader(header);
//    }

//    public static ChMessageHeader getChMessageHeaderMobile(final String mobileNo) {
//        HashMap<ChMessageHeader.HeaderContext, Object> header = new HashMap<>();
//        Map<ChMessageHeader.UserAgent, String> userAgentInfo = new HashMap();
//        userAgentInfo.put(UserAgent.IDENTIFICATION, mobileNo);
//        header.put(HeaderContext.USER_AGENT, userAgentInfo);
//        return new ChMessageHeader(header);
//    }

    public static ChMessageHeader getChMessageHeader(final HttpServletRequest request) {
        HashMap<ChMessageHeader.HeaderContext, Object> header = new HashMap();
        Map<ChMessageHeader.UserAgent, String> userAgentInfo = new HashMap();
        userAgentInfo.put(UserAgent.IDENTIFICATION, getMobileNo(request));
        userAgentInfo.put(UserAgent.USER_CLIENT_SPEC, getXForwardedForAddress(request));
        String operationSystemHeadersMap = getOperationSystemHeadersMap(request);
        if (operationSystemHeadersMap != null) {
            userAgentInfo.put(UserAgent.OPERATION_SYSTEM, operationSystemHeadersMap);
        }

        userAgentInfo.put(UserAgent.USER_CLIENT_IP, getClientIp(request));
        header.put(HeaderContext.USER_AGENT, userAgentInfo);
        return new ChMessageHeader(header);
    }

    public static ChMessageHeader getChMessageHeader(String channelServiceType) {
        HashMap<ChMessageHeader.HeaderContext, Object> header = new HashMap();
        header.put(HeaderContext.CHANNEL_SERVICE_TYPE, ChannelServiceType.valueOf(channelServiceType));
        header.put(HeaderContext.OMNI, Boolean.TRUE);
        return new ChMessageHeader(header);
    }

    private static String getOperationSystemHeadersMap(final HttpServletRequest request) {
        String osType = getHeader("osType", request);
        String osVersion = getHeader("osVersion", request);
        return osType == null && osVersion == null ? null : "{" + (osType != null ? "\"osType\":" + osType : "") + (osVersion != null ? ",\"osVersion\":" + osVersion : "") + "}";
    }

//    public static String getToken(final HttpServletRequest request) {
//        String token = null;
//        if (request.getHeader("Authorization") == null) {
//            token = request.getHeader("token");
//            if (token == null) {
//                token = request.getParameter("token");
//            }
//
//            return token;
//        } else {
//            token = request.getHeader("Authorization");
//            return token.replace("Bearer", "").trim();
//        }
//    }

    public static String getMobileNo(final HttpServletRequest request) {
        String mobileNo = null;
        mobileNo = request.getParameter("mobileNo");
        if (mobileNo == null) {
            mobileNo = request.getHeader("mobileNo");
        }

        return mobileNo;
    }

    public static String getXForwardedForAddress(final HttpServletRequest request) {
        String xForwardedFor = null;
        if (request.getHeader("x-forwarded-for") != null) {
            xForwardedFor = request.getHeader("x-forwarded-for");
        } else {
            xForwardedFor = "unknown";
        }

        return xForwardedFor;
    }

    public static String getClientIp(final HttpServletRequest request) {
        String clientIp = null;
        if (request.getHeader("clientIp") != null) {
            clientIp = request.getHeader("clientIp");
        }

        return clientIp;
    }

    public static String getHeader(final String headerName, final HttpServletRequest request) {
        return request.getHeader(headerName);
    }

    public static String getPichakChequeStatus(ChPichakChequeStatus pichakChequeStatus) {
        switch (pichakChequeStatus) {
            case ISSUED:
                return "ثبت شده با تغییر گیرنده";
            case PASSED:
                return "نقد شده";
            case CANCELED:
                return "باطل شده";
            case RETURNED:
                return "برگشت خورده";
            case PARTIAL_RETURNED:
                return "بخشی برگشت خورده";
            case AWAITING_GUARANTOR_SIGNATURE:
                return "در انتظار امضا ضامن";
            case AWAITING_RECEIVER_APPROVAL:
                return "در انتظار تایید گیرنده در کشیدن چک";
            case AWAITING_TRANSFER_RECEIVER_APPROVAL:
                return "در انتظار تایید گیرنده در انتقال چک";
            default:
                return "";
        }
    }

    public static String getPichakBlockStatus(ChPichakBlockStatus chPichakBlockStatus) {
        switch (chPichakBlockStatus) {
            case NOT_BLOCKED:
                return "چک مسدود نشده است";
            case TEMPORARILY_BLOCKED:
                return "مسدود موقت می باشد";
            case PERMANENTLY_BLOCKED:
                return "مسدود دائم می باشد";
            case BLOCK_REMOVED:
                return "چک رفع مسدودی شده است";
            default:
                return "";
        }
    }

    public static String getPichakGuaranteeStatus(ChPichakGuaranteeStatus chPichakGuaranteeStatus) {
        switch (chPichakGuaranteeStatus) {
            case NO_GUARANTEE:
                return "این چک فاقد ضمانت می باشد";
            case PROCESSING_IS_ONGOING:
                return "فرایند ضمانت در جریان است";
            case UNFINISHED_IS_OVER:
                return "فرایند ضمانت ناتمام خاتمه یافته است";
            case ALL_GUARANTOR_ACCEPTED:
                return "فرایند ضمانت اتمام و همه ضامن ها ضمانت کرده اند";
            case SOME_GUARANTOR_REJECTED:
                return "فرایند ضمانت اتمام و برخی ضامن ها ضمانت را رد کرده اند";
            default:
                return "";
        }
    }

    public static String getPichakCustomerType(ChPichakCustomerType pichakCustomerType) {
        switch (pichakCustomerType) {
            case IR_INDIVIDUAL:
                return "مشتری حقیقی";
            case IR_CORPORATE:
                return "مشتری حقوقی";
            case NON_IR_INDIVIDUAL:
                return "اتباع بیگانه حقیقی";
            case NON_IR_CORPORATE:
                return "اتباع بیگانه حقوقی";
            default:
                return "";
        }
    }

    public static String getPichakChequeType(ChPichakChequeType chPichakChequeType) {
        switch (chPichakChequeType) {
            case NORMAL:
                return "عادی";
            case BANKING:
                return "بانکی";
            case ENCRYPTED:
                return "رمزدار";
            default:
                return "";
        }
    }

    public static String getPichakChequeMediaType(ChPichakChequeMediaType chPichakChequeMediaType) {
        switch (chPichakChequeMediaType) {
            case PAPER_BASED:
                return "چک کاغذی";
            case DIGITAL:
                return "چک دیجیتال";
            default:
                return "";
        }
    }

    public String setDateFormat(String date) {
        StringBuilder builder = new StringBuilder();
        builder.append(date.substring(0, 4));
        builder.append("/");
        builder.append(date.substring(4, 6));
        builder.append("/");
        builder.append(date.substring(6, 8));
        return builder.toString();
    }

    public String getRejectCause(com.caspian.pichak.type.RejectCauseType rejectCauseType) {
        switch (rejectCauseType) {
            case ACCEPT:
                return "چک مورد تایید است";
            case MISTAKE_AMOUNT:
                return "مبلغ چک اشتباه است";
            case MISTAKE_DATE:
                return "تاریخ چک اشتباه است";
            case MISTAKE_INFO:
                return "اطلاعات پذیرنده چک اشتباه است";
            case MISTAKE_REGISTER:
                return "چک اشتباه ثبت شده است";
            case OTHER:
                return "سایر موارد";
            default:
                return "سایر موارد";
        }
    }

    public String getUsernameFromJwtToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && !token.contains("Basic")) {
            String[] token_part = token.split("\\.");
            String jwtToken = new String(Base64.getDecoder().decode(token_part[1]));
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject)gson.fromJson(jwtToken, JsonObject.class);
            return jsonObject.get("user_name").getAsString();
        } else {
            return "";
        }
    }
}

