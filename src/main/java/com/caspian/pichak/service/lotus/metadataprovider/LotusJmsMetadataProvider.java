package com.caspian.pichak.service.lotus.metadataprovider;

import com.caspian.banking.common.ChannelType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;



@Component
@Order(1)
public class LotusJmsMetadataProvider {
    @Value("${lotus.core.filter}")
    private String filter;
    @Value("${lotus.core.cryptography.lotus.host.alias}")
    private String securityAliasNam;

    @Value("${lotus.core.version}")
    private String clientVersion;
    @Value("${lotus.core.version}")
    private String gatewayVersion;

    @Value("${lotus.core.default.branchcode}")
    private String defaultBranchCode;
    @Value("${lotus.core.default.user}")
    private String defaultUser;
    @Value("${lotus.core.default.currency}")
    private String defaultCurrency;
    @Value("${lotus.core.default.channel}")
    private String channel;

    @Value("${core.rest.wrapper.jms.queue.timeout:300000}")
    private int jmsTimeout;
    @Value("${lotus.core.request.expiry.timeout:300000}")
    private int coreRequestExpiryTimeout;

    @Value("${gateway.spi.jms.connectionFactory}")
    private String factoryJndiName;
    @Value("${lotus.core.dateFormat:yyyy-MM-dd HH:mm:ss.S}")
    private String coreDateFormat;
    @Value("${gateway.spi.jms.requestQueue}")
    private String reqJndiName;
    @Value("${gateway.spi.jms.responseQueue}")
    private String resJndiName;
    @Value("${gateway.spi.jms.provider.url}")
    private String lotusWeblogicUrl;
    //------------------------------------------------------

    public String getFilter() {
        return filter.trim();
    }

    public String getClientVersion() {
        return clientVersion.trim();
    }

    public String getGatewayVersion() {
        return gatewayVersion.trim();
    }


    public String getDefaultBranchCode() {
        return defaultBranchCode.trim();
    }

    public String getDefaultUser() {
        return defaultUser.trim();
    }

    public String getDefaultCurrency() {
        return defaultCurrency.trim();
    }

    public int getCoreRequestExpiryTimeout() {
        return coreRequestExpiryTimeout;
    }

    public String getChannel() {
        return channel.trim();
    }

    public ChannelType getChannelType() {
        return ChannelType.valueOf(channel.trim());
    }

    public int getJmsTimeout() {
        return jmsTimeout;
    }

    public String getFactoryJndiName() {
        return factoryJndiName.trim();
    }

    public String getCoreDateFormat() {
        return coreDateFormat.trim();
    }

    public String getReqJndiName() {
        return reqJndiName.trim();
    }

    public String getResJndiName() {
        return resJndiName.trim();
    }

    public String getLotusWeblogicUrl() {
        return lotusWeblogicUrl.trim();
    }

    public String getSecurityAliasNam() {
        return securityAliasNam.trim();
    }
}
