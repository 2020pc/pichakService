package com.caspian.pichak.type;

import java.util.Arrays;

public enum NationalCodeType {
    INDIVIDUAL("1"),
    CORPORATE("2"),
    INDIVIDUAL_FOREIGNER("3"),
    CORPORATE_FOREIGNER("4");

    public String value;

    private NationalCodeType(String s) {
        this.value = s;
    }

    public static NationalCodeType fromValue(String s) {
        return (NationalCodeType) Arrays.stream(values()).filter((n) -> n.value.equals(s)).findFirst().get();
    }
}
