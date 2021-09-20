package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.request.SingleOrderRequest;
import com.sabi.agent.core.integrations.response.SingleOrderResponse;
import com.sabi.framework.helpers.API;
import com.sabi.framework.service.ExternalTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private API api;
    @Autowired
    private ExternalTokenService externalTokenService;
    @Value("${order.detail.url}")
    private String orderDetail;



    public SingleOrderResponse productDetail (SingleOrderRequest request) throws IOException {

        Map map=new HashMap();
        map.put("Authorization","bearer"+ " " +externalTokenService.getToken());
        SingleOrderResponse response = api.get(orderDetail + request.getId(), SingleOrderResponse.class,map);
        return response;
    }



}
