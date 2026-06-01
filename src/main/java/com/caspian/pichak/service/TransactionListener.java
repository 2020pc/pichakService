package com.caspian.pichak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@Component
public class TransactionListener implements ApplicationListener<ServletRequestHandledEvent> {
    @Autowired
    private RequestAttemptService requestAttemptService;

    public void onApplicationEvent(ServletRequestHandledEvent e) {
        this.requestAttemptService.requestAttempt(e.getClientAddress() + "-" + e.getRequestUrl());
    }
}
