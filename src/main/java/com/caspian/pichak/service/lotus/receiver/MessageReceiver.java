package com.caspian.pichak.service.lotus.receiver;


import com.caspian.banking.deposit.model.pichak.PichakExceptionModel;
import com.caspian.banking.exception.SystemException;
import com.caspian.banking.message.ResponseType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;



@Component
public class MessageReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);
    //------------------------------------------------------------------------------------------------
    private final Gson gson;

    public MessageReceiver() {
        this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.S").create();
    }

    public String formatMessageCorrelationID(final String messageCorrelationID) {
        return String.format("JMSCorrelationID='%s'", messageCorrelationID);
    }

    public <O> O handledBytesMessage(final javax.jms.Message receivedMessage, final Class<O> outboundClass) throws IOException, JMSException, SystemException {
        if (receivedMessage instanceof BytesMessage) {
            final ResponseType responseType = this.extractResponseType(receivedMessage);
            switch (responseType) {
                case FAILED: {
                    LOGGER.error("message received has responseType: FAILED");
                    this.handleFailed(receivedMessage);
                    break;
                }
                case INQUIRY:
                case PROCESSED: {
                    LOGGER.info("message received ok and has responseType:" + responseType.name());
                    return this.handleProcessed(receivedMessage, outboundClass);
                }
                default: {
                    LOGGER.error("{! CRITICAL ERROR !} Unsupported message ResponseType: " + ((responseType != null) ? responseType.name() : null));
                    throw new IllegalStateException("ResponseType of message not supported responseType: " + ((responseType != null) ? responseType.name() : null));
                }
            }
        }
        LOGGER.error("{! CRITICAL ERROR !} Unsupported message type(not BytesMessage): " + ((receivedMessage != null) ? receivedMessage.getClass().getName() : null));
        throw new IllegalStateException("receivedMessage is not BytesMessage and not supported receivedMessage type:" + ((receivedMessage != null) ? receivedMessage.getClass().getName() : null));
    }

    private void handleFailed(final javax.jms.Message receivedMessage) throws IOException, JMSException, SystemException {
        final String body = fetchAndHandle(receivedMessage);
        final SystemException systemException = new SystemException(body);
        try {
            final PichakExceptionModel map = this.gson.fromJson(body, PichakExceptionModel.class);
//            systemException.setExceptionModel(map);
        } catch (Exception e) {
            LOGGER.error("{! CRITICAL ERROR !} Can not convert exception model", e.getMessage());
            throw systemException;
        }
        throw systemException;
    }

    private <O> O handleProcessed(final javax.jms.Message receivedMessage, final Class<O> outboundClass) throws IOException, JMSException {
        final String extractMessageBody = this.fetchAndHandle(receivedMessage);
        //todo add mongo log
        LOGGER.debug("message body extract ok body: " + extractMessageBody);
        return this.gson.fromJson(extractMessageBody, outboundClass);
    }


    private ResponseType extractResponseType(final javax.jms.Message receivedMessage) throws JMSException {
        return ResponseType.valueOf(receivedMessage.getStringProperty("responseType"));
    }

    private String fetchAndHandle(final javax.jms.Message receive) throws IOException, JMSException {
        if (receive instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) receive;
            byte[] buffer = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(buffer);
            if (receive.getBooleanProperty("compressed")) {
                LOGGER.debug("receive message was compressed.");
                final String decompressed = handleCompressed(buffer);
                LOGGER.debug("receive message decompressed successfully.receive message is: " + decompressed);
                return decompressed;
            }
            final String json = new String(buffer, StandardCharsets.UTF_8);
            LOGGER.debug("receive message is: " + json);
            return json;
        } else {
            LOGGER.error("{! CRITICAL ERROR !} Unsupported message(not BytesMessage type): " + ((receive != null) ? receive.getClass().getName() : null));
            throw new IllegalStateException("receive is not BytesMessage and not supported receive type:" + ((receive != null) ? receive.getClass().getName() : null));
        }
    }

    private String handleCompressed(final byte[] buffer) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        final GZIPInputStream gis = new GZIPInputStream(bais);
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));

        StringBuilder outStr = new StringBuilder();
        String line;
        while ((line = bf.readLine()) != null) {
            outStr.append(line);
        }
        bais.close();
        gis.close();
        bf.close();
        return outStr.toString();
    }
}
