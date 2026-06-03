package com.caspian.pichak.service.lotus.messagecreatorprovider;


import com.caspian.pichak.service.lotus.marshaller.SimpleJsonMarshaller;
import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;
import com.caspian.pichak.service.lotus.provider.MessagePropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;


@Component
public class ByteMessageCreatorProviderImpl extends MessageCreatorProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteMessageCreatorProviderImpl.class);
    //-------------------------------------------------------------------------------------------
    private final com.caspian.pichak.service.lotus.provider.MessagePropertiesProvider messagePropertiesProvider;

    //-------------------------------- CONSTRUCTOR ----------------------------------------
    public ByteMessageCreatorProviderImpl(final MessagePropertiesProvider messagePropertiesProvider) {
        this.messagePropertiesProvider = messagePropertiesProvider;
    }

    //--------------------------------- MAIN API -------------------------------------------
    @Override
    public final <I> MessageCreator provide(final I inbound, final MessagePropertiesModel propertiesModel, final String messageCorrelationID) {
        MessageCreator messageCreator = session -> {
            final Message message = session.createBytesMessage();
            this.setMessageCorrelationID(message, messageCorrelationID);
            this.fullFillMessageProperties(message, propertiesModel, inbound);
            this.write(message, inbound);
            LOGGER.info("message was created.");
            return message;
        };
        return messageCreator;
    }

    /**
     * Marshal And Write Message
     *
     * @param message
     * @param inbound
     * @param <I>
     */

    @Override
    protected final <I> void write(final Message message, final I inbound) {
        if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] marshaled = SimpleJsonMarshaller.INSTANCE.marshal(inbound);
            try {
                bytesMessage.writeBytes(marshaled);
                LOGGER.info("message was written into message");
            } catch (JMSException e) {
                LOGGER.error("Failed to write into message.", e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected final MessagePropertiesProvider getMessagePropertiesProvider() {
        return messagePropertiesProvider;
    }
}
