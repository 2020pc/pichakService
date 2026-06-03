package com.caspian.pichak.service.lotus.processor;


import com.caspian.pichak.service.lotus.model.DefaultMessagePropertiesModelProvider;
import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Component
public class MessagePropertiesProcessorImpl extends MessagePropertiesProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePropertiesProcessorImpl.class);

    //------------------------------------------------------------------------------------------------
    protected final MessagePropertiesModel defaultMessagePropertiesModel;

    //-------------------------------- CONSTRUCTOR --------------------------------------
    public MessagePropertiesProcessorImpl(final DefaultMessagePropertiesModelProvider defaultMessagePropertiesModelProvider) {
        this.defaultMessagePropertiesModel = defaultMessagePropertiesModelProvider.getDefaultMessageProperties();
    }

    //-------------------------------- MAIN API -----------------------------------------
    @Override
    public <I> MessagePropertiesModel process(final MessagePropertiesModel messageProperties, final I inbound) {
        LOGGER.debug("Processing MessagePropertiesModel Started: messageProperties == null :" + (messageProperties == null) + ((messageProperties == null) ? "it will use DefaultMessagePropertiesModel and just ffServiceId for it" : "it will use input messageProperties and just fullFillEssentials property which are necessary for message properties"));
        return (messageProperties != null) ? this.fullFillEssentials(messageProperties, inbound)
                : this.fullFillDefaultMessagePropertiesModel(this.getDefaultMessagePropertiesModel(), inbound);
    }

    //-----------------------------------------------------------------------------------
    @Override
    protected void ffChannel(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getChannel() == null) {
            msgPropertiesModel.setChannel(this.getDefaultMessagePropertiesModel().getChannel());
        }
    }

    @Override
    protected void ffClientVersion(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getClientVersion() == null || msgPropertiesModel.getClientVersion().isEmpty()) {
            msgPropertiesModel.setClientVersion(this.getDefaultMessagePropertiesModel().getClientVersion());
        }
    }

    @Override
    protected void ffGatewayVersion(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getGatewayVersion() == null || msgPropertiesModel.getGatewayVersion().isEmpty()) {
            msgPropertiesModel.setGatewayVersion(this.getDefaultMessagePropertiesModel().getGatewayVersion());
        }
    }

    @Override
    protected void ffFilter(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getFilter() == null || msgPropertiesModel.getFilter().isEmpty()) {
            msgPropertiesModel.setFilter(this.getDefaultMessagePropertiesModel().getFilter());
        }
    }

    @Override
    protected void ffPayloadSchema(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getPayloadSchema() == null || msgPropertiesModel.getPayloadSchema().isEmpty()) {
            msgPropertiesModel.setPayloadSchema(this.getDefaultMessagePropertiesModel().getPayloadSchema());
        }
    }

    @Override
    protected void ffMessagePayloadSchema(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getMessagePayloadSchema() == null || msgPropertiesModel.getMessagePayloadSchema().isEmpty()) {
            msgPropertiesModel.setMessagePayloadSchema(this.getDefaultMessagePropertiesModel().getMessagePayloadSchema());
        }
    }

    @Override
    protected void ffMessageType(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getMessageType() == null) {
            msgPropertiesModel.setMessageType(this.getDefaultMessagePropertiesModel().getMessageType());
        }
    }

    @Override
    protected void ffSecurityAliasName(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getSecurityAliasName() == null || msgPropertiesModel.getSecurityAliasName().isEmpty()) {
            msgPropertiesModel.setSecurityAliasName(this.getDefaultMessagePropertiesModel().getSecurityAliasName());
        }
    }

    @Override
    protected void ffTransactionType(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getTransactionType() == null) {
            msgPropertiesModel.setTransactionType(this.getDefaultMessagePropertiesModel().getTransactionType());
        }
    }

    @Override
    protected void ffDefaultUser(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getDefaultUser() == null || msgPropertiesModel.getDefaultUser().isEmpty()) {
            msgPropertiesModel.setDefaultUser(this.getDefaultMessagePropertiesModel().getDefaultUser());
        }
    }

    @Override
    protected void ffDefaultBranchCode(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getDefaultBranchCode() == null || msgPropertiesModel.getDefaultBranchCode().isEmpty()) {
            msgPropertiesModel.setDefaultBranchCode(this.getDefaultMessagePropertiesModel().getDefaultBranchCode());
        }
    }

    @Override
    protected void ffDefaultCurrency(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getDefaultCurrency() == null || msgPropertiesModel.getDefaultCurrency().isEmpty()) {
            msgPropertiesModel.setDefaultCurrency(this.getDefaultMessagePropertiesModel().getDefaultCurrency());
        }
    }

    @Override
    protected void ffTransactionId(final MessagePropertiesModel msgPropertiesModel) {
        if (msgPropertiesModel.getTransactionId() == null || msgPropertiesModel.getTransactionId().isEmpty()) {
            msgPropertiesModel.setTransactionId(this.getDefaultMessagePropertiesModel().getTransactionId());
        }
    }

    @Override
    protected MessagePropertiesModel getDefaultMessagePropertiesModel() {
        return this.defaultMessagePropertiesModel;
    }
}
