package com.caspian.pichak.service.lotus.messagecreatorprovider;


import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;
import com.caspian.pichak.service.lotus.provider.MessagePropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;



public abstract class MessageCreatorProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCreatorProvider.class);

    //------------------------------------------------------------------------------------------------
    public abstract <I> MessageCreator provide(final I inbound, final MessagePropertiesModel propertiesModel, final String messageCorrelationID);

    protected abstract <I> void write(final Message message, final I inbound);

    protected abstract MessagePropertiesProvider getMessagePropertiesProvider();

    protected <I> void fullFillMessageProperties(final Message message, final MessagePropertiesModel propertiesModel, final I inbound) {
        this.getMessagePropertiesProvider().provide(message, propertiesModel, inbound);
    }

    protected void setMessageCorrelationID(final Message message, final String messageCorrelationID) {
        try {
            message.setJMSCorrelationID(messageCorrelationID);
        } catch (JMSException e) {
            LOGGER.error("Error in setting message correlationID: " + messageCorrelationID, e);
            throw new RuntimeException(e);
        }
    }
}
