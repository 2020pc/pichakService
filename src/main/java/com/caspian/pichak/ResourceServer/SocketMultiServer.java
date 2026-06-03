package com.caspian.pichak.ResourceServer;

import com.caspian.banking.ebanking.message.card.MGLoadCardCustomerInfoByNumberMsg;
import com.caspian.pichak.model.dto.ISOMessageDTO;
import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.utility.iso.IsoParser;
//import com.caspian.pichak.repository.ErrorDao;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pb.ouc.util.util.Utility;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class SocketMultiServer implements Runnable {
//    @Autowired
//    private ErrorDao errorDao;
    @Value("${tcp.server.port}")
    private Integer port;
    @Value("${tcp.service.timeout}")
    private Integer timeout;
    @Value("${service.id}")
    private Integer serviceId;
    @Value("${iso.key}")
    private String isoKey;
    @Value("${server.ssl.enabled}")
    private boolean serverSslEnabled;
    private ServerSocket serverSocket;
    private Utility utility;
    private String protocol;
    private Gson gson;

    @Autowired
    private ATMResource atmResource;
    @Autowired
    private PublicResource publicResource;

    public static void main(String[] args) {
        SocketMultiServer server = new SocketMultiServer();
        server.start(server.port);
    }

    public void start(int port) {
        (new Thread(() -> {
            try {
                this.serverSocket = new ServerSocket(port);
                AAAServer.logger.info("start tcp server on port: " + port);

                while(true) {
                    (new Thread(new SocketClientHandler(this.serverSocket.accept()))).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.stop();
            }

        })).start();
    }

    public void stop() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @PostConstruct
    public void run() {
        this.utility = new Utility();
        this.protocol = this.serverSslEnabled ? "https://" : "http://";
        this.gson = new Gson();
        this.start(this.port);
    }

    private String callService(ISOMessageDTO msg) throws Exception {
        String postUrl = this.protocol + "127.0.0.1:8082/digital/atm";
        String service;
        switch (msg.getServiceCode()) {
            case 2:
                return atmResource.chequeInquiry(msg);
            case 3:
            case 5:
            case 7:
            case 9:
            case 11:
            default:
                service = "";
                break;
            case 4:
                return atmResource.getCustomerInfo(msg);
            case 6:
                service = atmResource.chequeRegister(msg);
                break;
            case 8:
                service = atmResource.chequeAccept(msg);
                break;
            case 10:
                service = atmResource.chequeTransfer(msg);
                break;
            case 12:
                service = atmResource.getChequeAndDebtInquiry(msg);
        }

//        postUrl = postUrl.concat(service);
//        JsonObject request = msg.getBody();
//        request.addProperty("sessionId", msg.getSessionId());
//        request.addProperty("nationalCode", msg.getUser().getNationalCode());
//        request.addProperty("shahabCode", msg.getUser().getShahabCode());
//        request.addProperty("customerId", msg.getUser().getCustomerId());
//        request.addProperty("name", msg.getUser().getFirstName() + " " + msg.getUser().getLastName());
//        request.addProperty("clientType", msg.getUser().getClientType());
//        request.addProperty("rrn", msg.getRrn());
//        Map<String, String> headerDataMap = new HashMap();
//        headerDataMap.put("Content-Type", "application/json");
//        headerDataMap.put("Authorization", "Bearer " + msg.getUser().getAccessToken());
//        return utility.postCall(postUrl, request.toString(), headerDataMap, this.timeout);
        return null;
    }

//    private void getToken(ISOMessageDTO msg) throws Exception {
//
//
////        String tokenUrl = this.protocol + "127.0.0.1:8082/OTP/oauth2/token";
////        Map<String, String> headerDataMap = new HashMap();
////        headerDataMap.put("Content-Type", "application/x-www-form-urlencoded");
////        headerDataMap.put("Authorization", "Basic dGVzdDoxMTExMTE=");
////        headerDataMap.put("endpoint", "ATM");
////        String data = "username=ATM_PARSIAN&password=" + this.isoKey + "&grant_type=password&endpoint=atm";
////        String token = this.utility.postCall(tokenUrl, data, headerDataMap, this.timeout);
////        JsonObject jsonObject = (JsonObject)this.gson.fromJson(token, JsonObject.class);
//        String token = localTokenService.createToken(msg.getUser());
//
//        ISOMessageDTO.User user = msg.getUser();
//        user.setAccessToken(token);
//    }

    private void getLogin(ISOMessageDTO msg) throws Exception {
        String postUrl = protocol + "127.0.0.1:8082/digital/atm/login";
        Map<String, String> headerDataMap = new HashMap();
        headerDataMap.put("Content-Type", "application/json");
        headerDataMap.put("Authorization", "Bearer " + msg.getUser().getAccessToken());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mobile", msg.getUser().getMobile());
        jsonObject.addProperty("nationalId", msg.getUser().getNationalCode());
        jsonObject.addProperty("rrn", msg.getRrn());
        String response = this.utility.postCall(postUrl, jsonObject.toString(), headerDataMap, this.timeout);
        JsonObject object = gson.fromJson(response, JsonObject.class);
        JsonObject error = object.getAsJsonObject("error");
        if (error.get("code").getAsInt() != 0) {
            throw new Exception(error.toString());
        } else {
            JsonObject message = gson.fromJson(object.get("message").getAsString(), JsonObject.class);
            JsonObject chLoginResponseBean = message.get("chLoginResponseBean").getAsJsonObject();
            String sessionId = chLoginResponseBean.get("sessionId").getAsString();
            msg.setSessionId(sessionId);
        }
    }

    private void getUserInfo(ISOMessageDTO msg) throws Exception {

        MGLoadCardCustomerInfoByNumberMsg.Outbound customerInfo;
        try {

            customerInfo = publicResource.getCustomerInfoByPanCore(msg.getPan());
        } catch (Exception e) {
            throw new Exception(e.toString());
        }
        msg.getClass();
        ISOMessageDTO.User user = new ISOMessageDTO.User();
        user.setCustomerId(customerInfo.getCardCustomerInfoResponseDto().getCustomerId().toString());
        user.setFirstName(customerInfo.getCardCustomerInfoResponseDto().getFirstName());
        user.setLastName(customerInfo.getCardCustomerInfoResponseDto().getLastName());
        user.setNationalCode(customerInfo.getCardCustomerInfoResponseDto().getNationalCode());
        user.setClientType(customerInfo.getCardCustomerInfoResponseDto().getClientType().toString());
        user.setShahabCode(msg.getBody().get("shahabCode").getAsString());
        customerInfo.getContactInfoDTOS().stream().filter(x -> x.getContactType().equals("M")).findFirst().ifPresent(x -> {
            user.setMobile(x.getContactValue());
        });


//        JsonObject object = gson.fromJson(response, JsonObject.class);
//        JsonObject error = object.getAsJsonObject("error");
//        if (!error.get("code").getAsString().equals("0")) {
//            throw new Exception(error.toString());
//        } else {
//            JsonElement messageElement = gson.fromJson(object.get("message").getAsString(), JsonElement.class);
//            JsonObject message = messageElement.getAsJsonObject();
//            JsonObject chCardCustomerInfoResponseBean = message.get("cardCustomerInfoResponseDto").getAsJsonObject();
//            user.setCustomerId(chCardCustomerInfoResponseBean.get("customerId").getAsString());
//            user.setNationalCode(chCardCustomerInfoResponseBean.get("nationalCode").getAsString());
//            user.setShahabCode(message.get("shahabCode").getAsString());
//            user.setFirstName(chCardCustomerInfoResponseBean.get("firstName").getAsString());
//            user.setLastName(chCardCustomerInfoResponseBean.get("lastName").getAsString());
//            user.setClientType(chCardCustomerInfoResponseBean.get("clientType").getAsString());
//            JsonArray chContactInfoBeanList = message.get("contactInfoDTOS").getAsJsonArray();
//            chContactInfoBeanList.forEach((o) -> {
//                if (o.getAsJsonObject().get("contactType").getAsString().equals("M")) {
//                    user.setMobile(o.getAsJsonObject().get("contactValue").getAsString());
//                }
//            });
            msg.setUser(user);
//        }
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];

        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    static {
        String jssecacerts = "jssecacerts";
        if (Files.exists(Paths.get("/var/lotus/certs/jssecacerts"), new LinkOption[0])) {
            jssecacerts = "/var/lotus/certs/jssecacerts";
        }

//        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
//        System.setProperty("javax.net.ssl.keyStore", jssecacerts);
//        System.setProperty("javax.net.ssl.trustStore", jssecacerts);
//        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession sslSession) {
                if (hostname.equals("127.0.0.1")) {
                    System.out.println("Connect https to AAA is ok & hostname is " + hostname);
                    return true;
                } else if (hostname.equals("192.168.249.60")) {
                    System.out.println("Connect https to AAA is ok & hostname is " + hostname);
                    return true;
                } else if (hostname.equals("192.168.247.118")) {
                    System.out.println("Connect https to AAA is ok & hostname is " + hostname);
                    return true;
                } else if (hostname.equals("192.168.23.170")) {
                    System.out.println("Connect https to AAA is ok & hostname is " + hostname);
                    return true;
                } else {
                    System.out.println("Connect https is not ok & hostname is " + hostname);
                    return false;
                }
            }
        });
    }

    private class SocketClientHandler implements Runnable {
        private Socket clientSocket;
        private DataOutputStream out;
        private DataInputStream in;

        public SocketClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                this.out = new DataOutputStream(this.clientSocket.getOutputStream());
                this.in = new DataInputStream(new BufferedInputStream(this.clientSocket.getInputStream()));
                byte[] buffer = new byte[1024];
                int len = this.in.read(buffer, 0, buffer.length);
                AAAServer.logger.info("len data:" + len);
                if (len > 0) {
                    byte[] trimBuffer = Arrays.copyOf(buffer, len);
                    AAAServer.logger.info("tcp message is:" + new String(trimBuffer, StandardCharsets.UTF_8));
                    String responseMessage = null;
                    String errorCode = null;
                    IsoParser isoParser = new IsoParser(SocketMultiServer.this.isoKey);
                    ISOMessageDTO isoMessageDTO = null;

                    try {
                        isoMessageDTO = isoParser.parsIsoMessage(trimBuffer);
                        isoMessageDTO.setRrn(utility.getRRN(serviceId));
                        getUserInfo(isoMessageDTO);
//                        getToken(isoMessageDTO);
                        String response = callService(isoMessageDTO);
                        isoMessageDTO.setServiceCode(isoMessageDTO.getServiceCode());
                        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                        try {

                            errorCode = jsonObject.get("error").getAsJsonObject().get("code").getAsString();
                        }catch (IllegalStateException e){
                            errorCode = jsonObject.get("status").getAsString();
                        }
                        if (errorCode.equals("0")) {
                            responseMessage = jsonObject.get("message").getAsString().replaceAll("\"", "");
                        } else {
//                            errorCode = String.valueOf(errorDao.findByMessage(errorCode).getMessage());
                            errorCode = errorCode;
                        }

                        AAAServer.logger.info(responseMessage);
                    } catch (Exception e) {
                        AAAServer.logger.error("stack trace", e);
                        responseMessage = null;
                        if (e.getMessage().contains("timed out")) {
                            errorCode = "126";
                        } else {
                            errorCode = "127";
                        }
                    } finally {
                        byte[] e = null;
                        byte[] var25 = null;
                        this.out.write(isoParser.makeResponse(responseMessage, isoMessageDTO, errorCode));
                    }
                }
            } catch (Exception e) {
                AAAServer.logger.error(e.getMessage());
            } finally {
                this.closeSocket();
            }

        }

        private void closeSocket() {
            try {
                this.in.close();
                this.out.close();
                this.clientSocket.close();
                this.in = null;
                this.out = null;
                this.clientSocket = null;
            } catch (Exception e) {
                this.in = null;
                this.out = null;
                this.clientSocket = null;
                e.printStackTrace();
            }

        }
    }
}
