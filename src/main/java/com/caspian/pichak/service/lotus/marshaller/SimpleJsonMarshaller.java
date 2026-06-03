package com.caspian.pichak.service.lotus.marshaller;

import com.caspian.banking.message.format.Format;
import com.caspian.banking.message.format.JsonFormat;


public enum SimpleJsonMarshaller implements Format {
    INSTANCE;

    @Override
    public final byte[] marshal(final Object object) {
        final Format marshaller = new JsonFormat();
        final byte[] marshaled = marshaller.marshal(object);
        return marshaled;
    }

    @Override
    public final <T> T unmarshal(final byte[] bytes, final Class<T> typeClass) {
        final Format marshaller = new JsonFormat();
        final T t = marshaller.unmarshal(bytes, typeClass);
        return t;
    }
}
