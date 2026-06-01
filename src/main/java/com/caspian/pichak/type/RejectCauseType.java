package com.caspian.pichak.type;

import java.util.Arrays;

public enum RejectCauseType {
    ACCEPT("1"),
    MISTAKE_AMOUNT("2"),
    MISTAKE_INFO("3"),
    MISTAKE_DATE("4"),
    MISTAKE_REGISTER("5"),
    OTHER("6");

    public String value;

    private RejectCauseType(String s) {
        this.value = s;
    }

    public static RejectCauseType fromValue(String s) {
        return  Arrays.stream(values()).filter((n) -> n.value.equals(s)).findFirst().get();
    }
}
