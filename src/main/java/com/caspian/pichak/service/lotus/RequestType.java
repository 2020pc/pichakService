package com.caspian.pichak.service.lotus;

public enum RequestType {
    TRANSACTION(100),
    INQUIRY(200);

    private final Integer code;

    private RequestType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }
}
