package com.caspian.pichak.service.lotus.provider;

import com.caspian.pichak.service.lotus.model.MessagePropertiesModel;

import javax.jms.Message;


public abstract class MessagePropertiesProvider {
    public abstract <I> void provide(final Message message, final MessagePropertiesModel msgProperties, I inbound);

    protected abstract void addMessageProperties(final Message message, final MessagePropertiesModel messagePropertiesModel);
}
