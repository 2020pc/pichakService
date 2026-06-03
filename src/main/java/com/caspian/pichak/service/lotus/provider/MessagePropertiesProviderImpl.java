package com.caspian.pichak.service.lotus.provider;


import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;
import com.caspian.pichak.service.lotus.processor.MessagePropertiesProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;


@Component
public class MessagePropertiesProviderImpl extends MessagePropertiesProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePropertiesProviderImpl.class);
    //------------------------------------------------------------------------------------------------
    private final MessagePropertiesProcessor messagePropertiesProcessor;

    //-------------------------------- CONSTRUCTOR --------------------------------------
    public MessagePropertiesProviderImpl(final MessagePropertiesProcessor messagePropertiesProcessor) {
        this.messagePropertiesProcessor = messagePropertiesProcessor;
    }

    //-----------------------------------  MAIN API  ----------------------------------------
    @Override
    public final <I> void provide(final Message message, final MessagePropertiesModel msgProperties, final I inbound) {
        final MessagePropertiesModel messagePropertiesModel = messagePropertiesProcessor.process(msgProperties, inbound);
        LOGGER.debug("END Processing Message Property properties.");
        this.addMessageProperties(message, messagePropertiesModel);
    }

    /**
     * This Method Add properties to javax.jms.Message
     */
    @Override
    protected final void addMessageProperties(final Message message, final MessagePropertiesModel messagePropertiesModel) {
        Map<String, String> messagePropertiesModelMap = messagePropertiesModel.createMessagePropertiesModelMap();
        try {
            for (Map.Entry<String, String> entry : messagePropertiesModelMap.entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }
            LOGGER.debug("All Message Property properties added into Message.");
        } catch (JMSException e) {
            LOGGER.error("{! CRITICAL ERROR !} Cannot setStringProperty messagePropertiesModelMap into message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
