package com.caspian.pichak.service;

import com.caspian.moderngateway.spi.service.ChannelManagerProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

public class AAAService {
    @Autowired
    private ChannelManagerProvider provider;
    private Serializable message;

    public AAAService(Serializable message) {
        this.message = message;
    }
}
