package com.caspian.pichak.type;

import java.util.Arrays;

public enum ClientType {
    INDIVIDUAL("I"),
    CORPORATE("C"),
    INDIVIDUAL_FOREIGNER("F"),
    CORPORATE_FOREIGNER("CF");

    public String value;

    private ClientType(String s) {
        this.value = s;
    }

    public static ClientType fromValue(String s) {
        return (ClientType) Arrays.stream(values()).filter((n) -> n.value.equals(s)).findFirst().get();
    }
}
