package com.caspian.pichak.ResourceServer;

import com.caspian.banking.ebanking.message.card.MGLoadCardCustomerInfoByNumberMsg;
import com.caspian.pichak.model.dto.ISOMessageDTO;
import com.caspian.pichak.service.ResponseHelper;
import com.caspian.pichak.utility.AAAServer;
import com.caspian.pichak.utility.iso.IsoParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Service
public class SocketMultiServer implements Runnable {

    @Value("${tcp.server.port}")
    private Integer port;
      @Value("${service.id}")
    private Integer serviceId;
    @Value("${iso.key}")
    private String isoKey;
        private ServerSocket serverSocket;
    private Utility utility;
    private Gson gson;

    @Autowired
    private ATMResource atmResource;


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
        this.gson = new Gson();
        this.start(this.port);
    }

    private String callService(ISOMessageDTO msg) {
        long rrn = msg.getRrn();
        AAAServer.logger.info("Processing RRN {} - Service {}", rrn, msg.getServiceCode());

        JsonObject request = msg.getBody();
        request.addProperty("sessionId", msg.getSessionId());
        request.addProperty("nationalCode", msg.getUser().getNationalCode());
        request.addProperty("shahabCode", msg.getUser().getShahabCode());
        request.addProperty("customerId", msg.getUser().getCustomerId());
        request.addProperty("name", msg.getUser().getFirstName() + " " + msg.getUser().getLastName());
        request.addProperty("clientType", msg.getUser().getClientType());
        request.addProperty("rrn", msg.getRrn());
        try {
            String result = switch (msg.getServiceCode()) {
                case 2 -> atmResource.chequeInquiry(request.toString());
                case 4 -> atmResource.getCustomerInfo(request.toString());
                case 6 -> atmResource.chequeRegister(request.toString());
                case 8 -> atmResource.chequeAccept(request.toString());
                case 10 -> atmResource.chequeTransfer(request.toString());
                case 12 -> atmResource.getChequeAndDebtInquiry(request.toString());
                default -> ResponseHelper.createErrorResponse("-1", "Service not supported");
            };

            AAAServer.logger.info("RRN {} - Service {} completed", rrn, msg.getServiceCode());
            return result;

        } catch (Exception e) {
            AAAServer.logger.error("RRN {} - Service {} failed: {}", rrn, msg.getServiceCode(), e.getMessage(), e);
            return ResponseHelper.createErrorResponse("-1", "Internal error");
        }



    }

    private void getUserInfo(ISOMessageDTO msg) throws Exception {


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pan", msg.getPan());
        jsonObject.addProperty("rrn", msg.getRrn());
        String response = atmResource.getCustomerInfoByPanCore(jsonObject.toString());
        msg.getClass();
        ISOMessageDTO.User user = new ISOMessageDTO.User();
        JsonObject object = (JsonObject)this.gson.fromJson(response, JsonObject.class);
        JsonObject error = object.getAsJsonObject("error");
        if (!error.get("code").getAsString().equals("0")) {
            throw new Exception(error.toString());
        } else {
            JsonElement messageElement = (JsonElement)this.gson.fromJson(object.get("message").getAsString(), JsonElement.class);
            JsonObject message = messageElement.getAsJsonObject();
            JsonObject chCardCustomerInfoResponseBean = message.get("cardCustomerInfoResponseDto").getAsJsonObject();
            user.setCustomerId(chCardCustomerInfoResponseBean.get("customerId").getAsString());
            user.setNationalCode(chCardCustomerInfoResponseBean.get("nationalCode").getAsString());
            user.setShahabCode(message.get("shahabCode").getAsString());
            user.setFirstName(chCardCustomerInfoResponseBean.get("firstName").getAsString());
            user.setLastName(chCardCustomerInfoResponseBean.get("lastName").getAsString());
            user.setClientType(chCardCustomerInfoResponseBean.get("clientType").getAsString());
            JsonArray chContactInfoBeanList = message.get("contactInfoDTOS").getAsJsonArray();
            chContactInfoBeanList.forEach((o) -> {
                if (o.getAsJsonObject().get("contactType").getAsString().equals("M")) {
                    user.setMobile(o.getAsJsonObject().get("contactValue").getAsString());
                }
            });
            msg.setUser(user);
        }
    }




    static {

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
//
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

                        out.write(isoParser.makeResponse(responseMessage, isoMessageDTO, errorCode));
                    }
                }
            } catch (Exception e) {
                AAAServer.logger.error(e.getMessage());
            } finally {
                closeSocket();
            }

        }

        private void closeSocket() {
            try {
                in.close();
                out.close();
                clientSocket.close();
                in = null;
                out = null;
                clientSocket = null;
            } catch (Exception e) {
                AAAServer.logger.error("Error processing TCP message", e);
                in = null;
                out = null;
                clientSocket = null;
                e.printStackTrace();
            }

        }
    }
}
