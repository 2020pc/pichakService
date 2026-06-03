package com.caspian.pichak.service.lotus.constants;

public enum LotusConstant {
    DATE_FORMAT("yyyy-MM-dd HH:mm:ss.S");

    private String value;

    LotusConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
