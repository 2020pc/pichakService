package com.caspian.pichak.ResourceServer;


import com.caspian.moderngateway.spi.service.JmsChannelManagerProvider;
import com.caspian.pichak.service.lotus.LotusJmsService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Configurable
public class BaseResource {
    @Autowired
    protected LotusJmsService lotusJmsService;
    @Value("${lotus.core.username}")
    protected String coreUsername;
    @Value("${lotus.core.branchcode}")
    protected String coreBranchcode;
    @Autowired
    protected Gson gson;
    @Autowired
    protected JmsChannelManagerProvider provider;

    protected String makeResponse(Map map) {
        return this.gson.toJson(map);
    }


}
