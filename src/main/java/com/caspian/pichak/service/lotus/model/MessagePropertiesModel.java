package com.caspian.pichak.service.lotus.model;

import com.caspian.banking.common.ChannelType;
import com.caspian.banking.message.MessageType;
import com.caspian.banking.message.RequestType;
import com.caspian.banking.message.TransactionType;
import com.caspian.pichak.service.lotus.constants.LotusJmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class MessagePropertiesModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePropertiesModel.class);
    protected ChannelType channel;
    protected String clientVersion;
    protected String gatewayVersion;
    protected String filter;
    protected String serviceId;
    protected String payloadSchema;
    protected String messagePayloadSchema;
    protected MessageType messageType;
    protected RequestType requestType;
    protected String securityAliasName;
    protected TransactionType transactionType;
    protected String defaultBranchCode;
    protected String defaultUser;
    protected String defaultCurrency;
    protected String transactionId;

    //---------------------------------------------- Constructor -------------------------------------------------------
    protected MessagePropertiesModel() {
    }

    //----------------------------------------------- MAIN API ---------------------------------------------------------
    public Map<String, String> createMessagePropertiesModelMap() {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put(LotusJmsConstants.CHANNEL.getName(), isNull(this.getChannel()) ? null : this.getChannel().name());
        propertiesMap.put(LotusJmsConstants.CLIENT_VERSION.getName(), this.getClientVersion());
        propertiesMap.put(LotusJmsConstants.GATEWAY_VERSION.getName(), this.getGatewayVersion());
        propertiesMap.put(LotusJmsConstants.FILTER.getName(), this.getFilter());
        propertiesMap.put(LotusJmsConstants.SERVICE_ID.getName(), this.getServiceId());
        propertiesMap.put(LotusJmsConstants.PAYLOAD_SCHEMA.getName(), this.getPayloadSchema());
        propertiesMap.put(LotusJmsConstants.MESSAGE_PAYLOAD_SCHEMA.getName(), this.getMessagePayloadSchema());
        propertiesMap.put(LotusJmsConstants.MESSAGE_TYPE.getName(), isNull(this.getMessageType()) ? null : this.getMessageType().name());
        propertiesMap.put(LotusJmsConstants.REQUEST_TYPE.getName(), isNull(this.getRequestType()) ? null : this.getRequestType().name());
        propertiesMap.put(LotusJmsConstants.SECURITY_ALIAS_NAME.getName(), this.getSecurityAliasName());
        propertiesMap.put(LotusJmsConstants.TRANSACTION_TYPE.getName(), isNull(this.getTransactionType()) ? null : this.getTransactionType().name());
        propertiesMap.put(LotusJmsConstants.USER_CREDENTIALS.getName(), this.getUserCredentials());
        propertiesMap.put(LotusJmsConstants.TRANSACTION_ID.getName(), this.getTransactionId());
        return propertiesMap;
    }

    private <G> boolean isNull(final G obj) {
        return obj == null;
    }

    //-------------------------------------------- SETTER & GETTER -----------------------------------------------------
    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getGatewayVersion() {
        return gatewayVersion;
    }

    public void setGatewayVersion(String gatewayVersion) {
        this.gatewayVersion = gatewayVersion;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPayloadSchema() {
        return payloadSchema;
    }

    public void setPayloadSchema(String payloadSchema) {
        this.payloadSchema = payloadSchema;
    }

    public String getMessagePayloadSchema() {
        return messagePayloadSchema;
    }

    public void setMessagePayloadSchema(String messagePayloadSchema) {
        this.messagePayloadSchema = messagePayloadSchema;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getSecurityAliasName() {
        return securityAliasName;
    }

    public void setSecurityAliasName(String securityAliasName) {
        this.securityAliasName = securityAliasName;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public String getDefaultBranchCode() {
        return defaultBranchCode;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getUserCredentials() {
        if (getDefaultUser() == null || getDefaultBranchCode() == null || getDefaultCurrency() == null
                || getDefaultUser().isEmpty() || getDefaultBranchCode().isEmpty() || getDefaultCurrency().isEmpty()) {
            LOGGER.error("lotus.core.default.user or lotus.core.default.branchcode or lotus.core.default.currency is null||empty or set to null||empty! and we have this data from it : " + "getDefaultUser():" + getDefaultUser() + "\t" + "getDefaultBranchCode():" + getDefaultBranchCode() + "\t" + "getDefaultCurrency():" + getDefaultCurrency());
            throw new IllegalStateException("lotus.core.default.user or lotus.core.default.branchcode or lotus.core.default.currency is null||empty or set to null||empty! ");
        }
        return getDefaultUser() + "@" + getDefaultBranchCode() + ":" + getDefaultCurrency();
    }

    public void setDefaultUser(final String defaultUser) {
        if (defaultUser == null || defaultUser.isEmpty()) {
            return;
        }
        this.defaultUser = defaultUser;
    }

    public void setDefaultBranchCode(final String defaultBranchCode) {
        if (defaultBranchCode == null || defaultBranchCode.isEmpty()) {
            return;
        }
        this.defaultBranchCode = defaultBranchCode;
    }

    public void setDefaultCurrency(final String defaultCurrency) {
        if (defaultCurrency == null || defaultCurrency.isEmpty()) {
            return;
        }
        this.defaultCurrency = defaultCurrency;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    //------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------- BUILDER ----------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    public static class Builder {
        private final MessagePropertiesModel messageProperties;

        public Builder() {
            this.messageProperties = new MessagePropertiesModel();
        }

        public Builder setChannel(final ChannelType channel) {
            this.messageProperties.setChannel(channel);
            return this;
        }

        public Builder setClientVersion(final String clientVersion) {
            this.messageProperties.setClientVersion(clientVersion);
            return this;
        }

        public Builder setGatewayVersion(final String gatewayVersion) {
            this.messageProperties.setGatewayVersion(gatewayVersion);
            return this;
        }

        public Builder setFilter(final String filter) {
            this.messageProperties.setFilter(filter);
            return this;
        }

        public Builder setServiceId(final String serviceId) {
            this.messageProperties.setServiceId(serviceId);
            return this;
        }

        public Builder setPayloadSchema(final String payloadSchema) {
            this.messageProperties.setPayloadSchema(payloadSchema);
            return this;
        }

        public Builder setMessagePayloadSchema(final String messagePayloadSchema) {
            this.messageProperties.setMessagePayloadSchema(messagePayloadSchema);
            return this;
        }

        public Builder setMessageType(final MessageType messageType) {
            this.messageProperties.setMessageType(messageType);
            return this;
        }

        public Builder setRequestType(final RequestType requestType) {
            this.messageProperties.setRequestType(requestType);
            return this;
        }

        public Builder setSecurityAliasName(final String securityAliasName) {
            this.messageProperties.setSecurityAliasName(securityAliasName);
            return this;
        }

        public Builder setTransactionType(final TransactionType transactionType) {
            this.messageProperties.setTransactionType(transactionType);
            return this;
        }

        public Builder setDefaultUser(final String defaultUser) {
            this.messageProperties.setDefaultUser(defaultUser);
            return this;
        }

        public Builder setDefaultBranchCode(final String defaultBranchCode) {
            this.messageProperties.setDefaultBranchCode(defaultBranchCode);
            return this;
        }

        public Builder setDefaultCurrency(final String defaultCurrency) {
            this.messageProperties.setDefaultCurrency(defaultCurrency);
            return this;
        }

        public Builder setTransactionId(final String transactionId) {
            this.messageProperties.setTransactionId(transactionId);
            return this;
        }

        public MessagePropertiesModel build() {
            return messageProperties;
        }
    }
}
