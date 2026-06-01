package com.caspian.pichak.service.lotus;

public enum MessageType {
    REQUEST(100),
    RESPONSE(200),
    NOTIFICATION(300);

    private final Integer code;

    private MessageType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }
}
