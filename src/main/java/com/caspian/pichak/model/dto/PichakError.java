package com.caspian.pichak.model.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.util.StringUtils;

public class PichakError  {
    private String code;
    private String message;

    public PichakError() {
        this.code = "0";
    }

    public PichakError(Exception e) {
        this.code = "-1";
        this.message = e.getMessage();
        if (StringUtils.isEmpty(this.message)) {
            this.message = "empty";
        }

    }

    public PichakError(String code, String message, String errorCodeNull, Boolean errorCodeLatin) {
//        AAAServer.logger.info("Exception is:\ncode: " + code + "\nmessage: " + message);
        this.code = code;
        this.message = message;
        if (message.contains("Invalid SessionId")) {
            this.code = "401";
        }

        if (!errorCodeLatin) {
            this.message = message.replaceAll("[^\\p{InARABIC}\\s]", "");
        }

        if (StringUtils.isEmpty(message)) {
            this.message = "خطای ارتباطی رخ داده است خواهشمند است برای استفاده از سامانه چند لحظه دیگر مجددا تلاش نمایید";
        }

    }

    public PichakError(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public PichakError(String jsonMessage) {
        JsonObject ibanObject = (JsonObject)(new Gson()).fromJson(jsonMessage, JsonObject.class);
        JsonElement errorElement = ibanObject.get("error");
        if (errorElement == null) {
            errorElement = ibanObject.get("messages");
            if (errorElement != null && errorElement.isJsonArray()) {
                this.message = errorElement.getAsJsonArray().get(0).getAsString();
                this.code = errorElement.getAsJsonArray().get(1).getAsString();
            }
        } else {
            JsonElement messageElement = errorElement.getAsJsonObject().get("message");
            JsonElement codeElement = errorElement.getAsJsonObject().get("code");
            if (messageElement != null) {
                this.message = messageElement.getAsString();
            }

            if (codeElement != null) {
                this.code = codeElement.getAsString();
            }
        }

    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
