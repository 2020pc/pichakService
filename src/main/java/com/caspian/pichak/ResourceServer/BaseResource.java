package com.caspian.pichak.ResourceServer;


import com.caspian.moderngateway.spi.service.JmsChannelManagerProvider;
import com.caspian.pichak.service.lotus.LotusJmsService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    public static void main(String[] args) {
        String myJSONString = "{'test': '100.00'}";
        BaseResource baseResource = new BaseResource();
        System.out.println(baseResource.getObjectFromMessage(myJSONString, "test"));
    }

    protected String getObjectFromMessage(String message, String key) {
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        return jsonObject.get(key).getAsString();
    }

    protected String makeResponse(Map map) {
        return this.gson.toJson(map);
    }

    protected String makeResponse(Object o) {
        return this.gson.toJson(o);
    }
}
