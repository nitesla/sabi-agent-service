package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.order.*;
import com.sabi.agent.core.integrations.order.orderResponse.CompleteOrderResponse;
import com.sabi.agent.core.integrations.order.orderResponse.CompleteOrderResponse;
import com.sabi.agent.core.integrations.order.orderResponse.CreateOrderResponse;
import com.sabi.agent.core.integrations.request.CompleteOrderRequest;
import com.sabi.agent.core.integrations.request.LocalCompleteOrderRequest;
import com.sabi.agent.core.integrations.request.MerchBuyRequest;
import com.sabi.agent.core.integrations.response.MerchBuyResponse;
import com.sabi.agent.core.models.AgentOrder;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.OrderRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.exceptions.ProcessingException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.integrations.payment_integration.models.response.PaymentStatusResponse;
import com.sabi.framework.repositories.PaymentDetailRepository;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.service.PaymentService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
@Service
@Slf4j
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

    @Value("${merchantbuy.url}")
    private String merchBuyUrl;

    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    public CreateOrderResponse placeOrder (PlaceOrder request) throws IOException {
        validations.validateOrderRequest(request);

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
        if (response.isStatus())
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

    public MerchBuyResponse merchBuy(MerchBuyRequest request){
        Map<String, String> map =new HashMap<>();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        log.info("Merchant buy url " + merchBuyUrl);
        return  api.post(merchBuyUrl, request,  MerchBuyResponse.class);
    }

    public AgentOrder findById(long id){
        return orderRepository.findById(id).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Requested bank id does not exist!"));
    }




    private void saveOrder(PlaceOrder request, CreateOrderResponse response) {

        AgentOrder order = AgentOrder.builder()
                .createdDate(new Date())
                .status(response.isStatus())
                .orderStatus("PROCESSING")
                .isSentToThirdParty(false)
                .agentId(request.getAgentId())
                .merchantId(request.getMerchantId())
                .orderId(Long.valueOf(response.getData().getOrderDelivery().getOrderId()))
                .totalAmount(String.valueOf(request.getOrderDelivery().getTotal()))
                .userName(response.getData().getUserName())
                .build();
        log.info("validating order " + request);
        validations.validateOrder(request);
        log.info("::::::::::::ORDER REQUEST::::::::::::::::: " + order);
        orderRepository.save(order);
    }



    public Page<AgentOrder> findAll(Long orderId, Boolean status, Date createdDate,Long agentId, String userName,PageRequest pageRequest) {
        Page<AgentOrder> agentOrder = orderRepository.findOrders(orderId,status,createdDate,agentId, userName,pageRequest);
        if (agentOrder == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentOrder;
    }

    public Page<Map<String, Object>> multiSearch(String searchTerm, String startDate, String endDate, PageRequest pageRequest){
        Page<Map<String, Object>> objects;

        if(startDate != null && endDate !=null ) {
            try {
                tryParseDate(startDate);
                tryParseDate(endDate);
                log.info("logging with date");
                objects = orderRepository.singleSearch(searchTerm, startDate, endDate, pageRequest);
            } catch (ParseException e) {
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Expected date format is yyyy-MM-dd HH:mm:ss");
            }
        }
        else
        objects = orderRepository.singleSearch(searchTerm, pageRequest);

        log.info("No. Of items from search " + objects.stream().count());
        return objects;
    }

    private void tryParseDate(String date) throws ParseException {
         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
    }

    public void localCompleteOrder(LocalCompleteOrderRequest completeOrderRequest){
        AgentOrder agentOrder = orderRepository.findById(completeOrderRequest.getOrderId()).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Enter a valid order id"));

        PaymentStatusResponse paymentStatusResponse = paymentService.checkStatus(completeOrderRequest.getPaymentReference());
        paymentStatusResponse.getPaymentDetails().setOrderId(agentOrder.getOrderId());
        agentOrder.setOrderStatus("PAID");
        agentOrder.setOrderDate(new Date());
        agentOrder.setSentToThirdParty(false);
        agentOrder.setThirdPartyResponseCode(completeOrderRequest.getCode());
        agentOrder.setThirdPartyResponseDesc(completeOrderRequest.getMessage());

        String paymentMethod = getPaymentMethod(completeOrderRequest.getPaymentMethod());

        if (paymentMethod.equals("NA")){
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Payment method not found");
        }

        agentOrder.setPaymentMethod(paymentMethod);
        agentOrder.setSuccessPaymentId(paymentStatusResponse.getPaymentDetails().getId());
        log.info("Updating agent order {} ", agentOrder);
        orderRepository.save(agentOrder);
        log.info("Updating payment {} for order {} ",paymentStatusResponse.getPaymentDetails(), agentOrder);
        paymentService.updatePaymentStatus(paymentStatusResponse);

    }

    public String getPaymentMethod(int paymentMethod){
        //PayOnDelivery = 1,
        //PayOnline = 2,
        //PayWithWallet = 3,
        //PostPaid = 4,
        //PayWithTransfer = 5
        String paymentMethodString = "";
        switch (paymentMethod){
            case 0:
                throw new ProcessingException("Error processing payment. Payment method invalid");
            case 1:
                paymentMethodString = "Pay on Delivery";
                break;
            case 2:
                paymentMethodString = "Pay online";
                break;
            case 3:
                paymentMethodString = "Pay with wallet";
                break;
            case 4:
                paymentMethodString = "Post Paid";
                break;
            case 5:
                paymentMethodString = "Pay with transefer";
                break;
            default:
                paymentMethodString = "NA";
        }
        return paymentMethodString;
    }


    public CompleteOrderResponse completeOrder(CompleteOrderRequest request){
        Map map = new HashMap();
        CompleteOrderRequest completeOrderRequest = new CompleteOrderRequest();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        return api.post(orderDetail + "transaction", request, CompleteOrderResponse.class, map);
    }
}
