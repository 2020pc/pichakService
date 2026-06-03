package com.caspian.pichak.service.lotus.constants;


public enum LotusJmsConstants {
    CHANNEL("channel"),
    CLIENT_VERSION("clientVersion"),
    GATEWAY_VERSION("gatewayVersion"),
    FILTER("filter"),
    SERVICE_ID("serviceId"),
    PAYLOAD_SCHEMA("payloadSchema"),
    MESSAGE_PAYLOAD_SCHEMA("messagePayloadSchema"),
    MESSAGE_TYPE("messageType"),
    REQUEST_TYPE("requestType"),
    SECURITY_ALIAS_NAME("securityAliasName"),
    TRANSACTION_TYPE("transactionType"),
    USER_CREDENTIALS("userCredentials"),
    TRANSACTION_ID("transactionId"),
    COMPRESSED("compressed");
    private final String name;

    public String getName() {
        return name;
    }

    LotusJmsConstants(String name) {
        this.name = name;
    }
}
