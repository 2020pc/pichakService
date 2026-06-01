package com.caspian.pichak.model.dto;

import com.google.gson.Gson;

public class ResponseMessage {

    private PichakError error;
    private Object message;

    public void setMessage(Object message) {
        this.message = (new Gson()).toJson(message);
    }

    public String toString() {
        return (new Gson()).toJson(this);
    }

    public PichakError getError() {
        return this.error;
    }

    public Object getMessage() {
        return this.message;
    }

    public void setError(final PichakError error) {
        this.error = error;
    }
}
