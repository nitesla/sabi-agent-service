package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.order.*;
import com.sabi.agent.core.integrations.order.orderResponse.CreateOrderResponse;
import com.sabi.framework.helpers.API;
import com.sabi.framework.service.ExternalTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private API api;
    @Autowired
    private ExternalTokenService externalTokenService;
    @Value("${order.history.url}")
    private String orderHistory;
    @Value("${order.url}")
    private String orderDetail;

    @Value("${create.order}")
    private String processOrder;

    @Value("${finger.print}")
    private String fingerPrint;



    public CreateOrderResponse placeOrder (PlaceOrder request) throws IOException {

        Map map=new HashMap();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        CreateOrderResponse response = api.post(processOrder ,request, CreateOrderResponse.class,map);
        return response;
    }




    public SingleOrderResponse orderDetail (SingleOrderRequest request) throws IOException {

        Map map=new HashMap();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        SingleOrderResponse response = api.get(orderDetail + request.getId(), SingleOrderResponse.class,map);
        return response;
    }


    public OrderHistoryResponse orderHistory (OrderHistoryRequest request) throws IOException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderHistory)
                // Add query parameter
                .queryParam("PageNumber",request.getPageNumber())
                .queryParam("PageSize",request.getPageSize());

        Map map=new HashMap();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        OrderHistoryResponse response = api.get(builder.toUriString(), OrderHistoryResponse.class,map);
        return response;
    }




}
