package com.caspian.pichak.service.lotus;

import com.caspian.banking.message.annotation.Message;

import java.util.Random;


public enum MessageHelper {
    INSTANCE;

    public String createRandomString() {
        return Long.toHexString(new Random().nextLong());
    }

    public long createRandomLong() {
        return new Random().nextLong();
    }

    public <I, O> Class<O> getOutboundClass(final I inbound) {
        final Class<?> messageClass = getMessageClassFromInbound(inbound);
        return this.getOutboundClassFromMessage(messageClass);
    }

    public <I> String getIdentifier(final I inbound) {
        return getIdentifierFromMessageClass(getMessageClassFromInbound(inbound));
    }

    public <I> boolean isInquiry(final I inbound) {
        if (inbound == null) {
            throw new IllegalArgumentException("inbound is null");
        }
        final Class<?> messageClass = this.getMessageClassFromInbound(inbound);
        final Message messageAnnotation = this.getMessageAnnotation(messageClass);
        if (messageAnnotation == null) {
            throw new IllegalArgumentException("@Message Annotation is Missing on: " + messageClass.getName());
        }
        return messageAnnotation.isInquiry();
    }

    private <I> Class<?> getMessageClassFromInbound(final I inbound) {
        if (inbound == null) {
            throw new IllegalArgumentException("inbound is null");
        }
        final Class<?> messageClass = inbound.getClass().getEnclosingClass();
        return messageClass;
    }

    private String getIdentifierFromMessageClass(final Class<?> messageClass) {
        if (messageClass == null) {
            throw new IllegalArgumentException("Message class is Null");
        }
        final Message messageAnnotation = this.getMessageAnnotation(messageClass);
        if (messageAnnotation == null) {
            throw new IllegalArgumentException("@Message Annotation is Missing on: " + messageClass.getName());
        }
        return messageAnnotation.identifier();
    }

    private Message getMessageAnnotation(final Class<?> messageClass) {
        if (messageClass == null) {
            throw new IllegalArgumentException("Message class is Null");
        }
        if (messageClass.isAnnotationPresent(Message.class)) {
            return messageClass.getAnnotation(Message.class);
        }
        return null;
    }

    private boolean isOutboundClass(final Class<?> outboundClass) {
        if (outboundClass.isAnnotationPresent(com.caspian.banking.message.annotation.Outbound.class)) {
            return true;
        } else if (outboundClass.getSimpleName().equalsIgnoreCase("outbound")) {
            return true;
        }
        return false;
    }

    private <M, O> Class<O> getOutboundClassFromMessage(final Class<M> messageClass) {
        final Class<?>[] innerClassArray = messageClass.getDeclaredClasses();
        for (Class innerclass : innerClassArray) {
            if (isOutboundClass(innerclass)) {
                return innerclass;
            }
        }
        throw new IllegalArgumentException("Invalid Outbound Class ->" + "Could Not Find outbound inner class in messageClass:" + messageClass.getName());
    }
}
