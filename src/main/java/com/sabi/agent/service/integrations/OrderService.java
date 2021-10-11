package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.order.*;
import com.sabi.agent.core.integrations.order.orderResponse.CreateOrderResponse;
import com.sabi.agent.core.models.AgentOrder;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.OrderRepository;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.utils.CustomResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private API api;
    @Autowired
    private ExternalTokenService externalTokenService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private Validations validations;
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
        PlaceOrder placeOrder = PlaceOrder.builder()
                .checkoutUserType(request.getCheckoutUserType())
                .customerComment(request.getCustomerComment())
                .location(request.getLocation())
                .orderDelivery(request.getOrderDelivery())
                .products(request.getProducts())
                .build();

        CreateOrderResponse response = api.post(processOrder ,placeOrder, CreateOrderResponse.class,map);
        saveOrder(request,response);
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




    private void saveOrder(PlaceOrder request, CreateOrderResponse response) {

        AgentOrder order = AgentOrder.builder()
                .createdDate(new Date())
                .status(response.isStatus())
                .agentId(request.getAgentId())
                .orderId(Long.valueOf(response.getData().getOrderDelivery().getOrderId()))
                .totalAmount(Long.valueOf(request.getOrderDelivery().getTotal()))
                .userName(response.getData().getUserName())
                .build();
        validations.validateOrder(request);
        orderRepository.save(order);

    }



    public Page<AgentOrder> findAll(Long orderId, Boolean status, Date createdDate,Long agentId, PageRequest pageRequest ) {
        Page<AgentOrder> agentOrder = orderRepository.findOrders(orderId,status,createdDate,agentId, pageRequest);
        if (agentOrder == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentOrder;
    }




}
