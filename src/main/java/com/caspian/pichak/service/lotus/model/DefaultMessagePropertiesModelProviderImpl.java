package com.caspian.pichak.service.lotus.model;


import com.caspian.banking.message.MessageType;
import com.caspian.banking.message.TransactionType;
import com.caspian.banking.message.format.FormatFactory;
import com.caspian.pichak.service.lotus.MessageHelper;
import com.caspian.pichak.service.lotus.metadataprovider.LotusJmsMetadataProvider;
import org.springframework.stereotype.Component;



@Component
public class DefaultMessagePropertiesModelProviderImpl extends DefaultMessagePropertiesModelProvider {
    private final LotusJmsMetadataProvider lotusJmsMetadataProvider;
    private final MessagePropertiesModel defaultMessageProperties;

    //-----------------------------CONSTRUCTOR------------------------------------------
    public DefaultMessagePropertiesModelProviderImpl(final LotusJmsMetadataProvider lotusJmsMetadataProvider) {
        this.lotusJmsMetadataProvider = lotusJmsMetadataProvider;
        this.defaultMessageProperties = provide();
    }

    //-----------------------------------  MAIN API  ----------------------------------------
    protected final MessagePropertiesModel provide() {
        return new MessagePropertiesModel.Builder()
                .setChannel(lotusJmsMetadataProvider.getChannelType())
                .setClientVersion(lotusJmsMetadataProvider.getClientVersion())
                .setGatewayVersion(lotusJmsMetadataProvider.getGatewayVersion())
                .setFilter(lotusJmsMetadataProvider.getFilter())
                //serviceId
                .setPayloadSchema(FormatFactory.FORMAT_JSONOBJECT)
                .setMessagePayloadSchema(FormatFactory.FORMAT_JSONOBJECT)
                .setMessageType(MessageType.REQUEST)
                //it will refill again with message isINQUIRY annotation
                //.setRequestType(RequestType.INQUIRY)
                .setSecurityAliasName(lotusJmsMetadataProvider.getSecurityAliasNam())
                .setTransactionType(TransactionType.INPUT)
                .setDefaultUser(lotusJmsMetadataProvider.getDefaultUser())
                .setDefaultBranchCode(lotusJmsMetadataProvider.getDefaultBranchCode())
                .setDefaultCurrency(lotusJmsMetadataProvider.getDefaultCurrency())
                .setTransactionId(MessageHelper.INSTANCE.createRandomString())
                .build();
    }

    //----------------------------------GETTER-------------------------------------------
    public final MessagePropertiesModel getDefaultMessageProperties() {
        return this.defaultMessageProperties;
    }
}
