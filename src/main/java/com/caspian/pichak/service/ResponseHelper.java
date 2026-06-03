package com.caspian.pichak.service;

import com.caspian.pichak.model.dto.ISOMessageDTO;
import com.caspian.pichak.model.dto.PichakError;
import com.caspian.pichak.model.dto.ResponseMessage;
import org.springframework.stereotype.Component;

@Component
public class ResponseHelper {
    public static String createResponse(Object result) {
        ResponseMessage response = new ResponseMessage();
        response.setError(new PichakError("0", null));
        response.setMessage(result);
        return response.toString();
    }

    public static String createErrorResponse(String code, String message) {
        ResponseMessage response = new ResponseMessage();
        response.setError(new PichakError(code, message));
        return response.toString();
    }

    public static String createErrorResponse(Exception e) {
        ResponseMessage response = new ResponseMessage();
        response.setError(new PichakError(e));
        return response.toString();
    }
}