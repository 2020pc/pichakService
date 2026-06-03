package com.caspian.pichak.service.lotus.processor;


import com.caspian.banking.message.RequestType;
import com.caspian.pichak.service.lotus.MessageHelper;
import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class MessagePropertiesProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePropertiesProcessor.class);

    //------------------------------------------------------------------------------------------------
    public abstract <I> MessagePropertiesModel process(final MessagePropertiesModel messageProperties, final I inbound);

    protected <I> MessagePropertiesModel fullFillEssentials(final MessagePropertiesModel msgPropertiesModel, final I inbound) {
        ffChannel(msgPropertiesModel);
        ffClientVersion(msgPropertiesModel);
        ffGatewayVersion(msgPropertiesModel);
        ffFilter(msgPropertiesModel);
        //*******************************************************************
        //This method uses reflections.It has difference from the other ones.
        ffServiceId(msgPropertiesModel, inbound);
        //This method uses reflections.It has difference from the other ones.
        ffRequestType(msgPropertiesModel, inbound);
        //*******************************************************************
        ffPayloadSchema(msgPropertiesModel);
        ffMessagePayloadSchema(msgPropertiesModel);
        ffMessageType(msgPropertiesModel);
        ffSecurityAliasName(msgPropertiesModel);
        ffTransactionType(msgPropertiesModel);

        ffDefaultUser(msgPropertiesModel);
        ffDefaultBranchCode(msgPropertiesModel);
        ffDefaultCurrency(msgPropertiesModel);

        ffTransactionId(msgPropertiesModel);
        LOGGER.debug("End fullFillEssentials for msgPropertiesModel: " + msgPropertiesModel.createMessagePropertiesModelMap().toString());
        return msgPropertiesModel;
    }

    protected <I> MessagePropertiesModel fullFillDefaultMessagePropertiesModel(final MessagePropertiesModel msgPropertiesModel, final I inbound) {
        //*******************************************************************
        //This method uses reflections.It has difference from the other ones.
        ffServiceId(msgPropertiesModel, inbound);
        //This method uses reflections.It has difference from the other ones.
        ffRequestType(msgPropertiesModel, inbound);
        //*******************************************************************
        return msgPropertiesModel;
    }

    protected <I> MessagePropertiesModel ffServiceId(final MessagePropertiesModel msgPropertiesModel, final I inbound) {
        final String identifier = MessageHelper.INSTANCE.getIdentifier(inbound);
        msgPropertiesModel.setServiceId(identifier);
        LOGGER.info("full fill msgPropertiesModel.ServiceId with (inbound message identifier is: " + identifier + ")");
        return msgPropertiesModel;
    }

    protected <I> MessagePropertiesModel ffRequestType(final MessagePropertiesModel msgPropertiesModel, final I inbound) {
        if (msgPropertiesModel.getRequestType() == null) {
            final boolean isInquiry = MessageHelper.INSTANCE.isInquiry(inbound);
            msgPropertiesModel.setRequestType(isInquiry ? RequestType.INQUIRY : RequestType.TRANSACTION);
            LOGGER.info("msgPropertiesModel.RequestType is null. full fill RequestType with isInquiry of inbound message annotation which is: " + isInquiry + ")");
            return msgPropertiesModel;
        }
        LOGGER.info("msgPropertiesModel.RequestType is: " + (msgPropertiesModel != null ? msgPropertiesModel.getRequestType() : null));
        return msgPropertiesModel;
    }

    protected abstract void ffChannel(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffClientVersion(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffGatewayVersion(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffFilter(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffPayloadSchema(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffMessagePayloadSchema(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffMessageType(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffSecurityAliasName(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffTransactionType(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffDefaultUser(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffDefaultBranchCode(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffDefaultCurrency(final MessagePropertiesModel msgPropertiesModel);

    protected abstract void ffTransactionId(final MessagePropertiesModel msgPropertiesModel);

    protected abstract MessagePropertiesModel getDefaultMessagePropertiesModel();
}
