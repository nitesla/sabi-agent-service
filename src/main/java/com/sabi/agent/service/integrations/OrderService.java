package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.order.*;
import com.sabi.agent.core.integrations.order.orderResponse.CompleteOrderResponse;
import com.sabi.agent.core.integrations.order.orderResponse.CreateOrderResponse;
import com.sabi.agent.core.integrations.request.CompleteOrderRequest;
import com.sabi.agent.core.integrations.request.LocalCompleteOrderRequest;
import com.sabi.agent.core.integrations.request.MerchBuyRequest;
import com.sabi.agent.core.integrations.response.LocalCompleteOrderResponse;
import com.sabi.agent.core.integrations.response.MerchBuyResponse;
import com.sabi.agent.core.integrations.response.Payment;
import com.sabi.agent.core.models.AgentOrder;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.OrderRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.integrations.payment_integration.models.response.PaymentStatusResponse;
import com.sabi.framework.models.PaymentDetails;
import com.sabi.framework.repositories.PaymentDetailRepository;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.service.PaymentService;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("ALL")
@Service
@Slf4j
@EnableAsync
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

    @Value("${merch.comm.order.base.url}")
    private String merchCommOrderBaseUrl;

    private final PaymentService paymentService;
    private final PaymentDetailRepository paymentDetailRepository;

    public OrderService(PaymentService paymentService, PaymentDetailRepository paymentDetailRepository) {
        this.paymentService = paymentService;
        this.paymentDetailRepository = paymentDetailRepository;
    }


    public CreateOrderResponse placeOrder(PlaceOrder request) throws IOException {
        validations.validateOrderRequest(request);
        String paymentMethod = paymentMethodString(request.getPaymentMethod());

        if (paymentMethod.equals("NA")) {
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Payment method not found");
        }

        Map map = new HashMap();
        map.put("fingerprint", fingerPrint);
        map.put("Authorization", "Bearer" + " " + externalTokenService.getToken());
        PlaceOrder placeOrder = PlaceOrder.builder()

                .checkoutUserType(request.getCheckoutUserType())
                .customerComment(request.getCustomerComment())
                .location(request.getLocation())
                .orderDelivery(request.getOrderDelivery())
                .products(request.getProducts())
                .build();
        CreateOrderResponse response = api.post(processOrder, placeOrder, CreateOrderResponse.class, map);
        if (response.isStatus())
            saveOrder(request, response, paymentMethod);
        return response;

    }

    @Deprecated
    public SingleOrderResponse orderDetail(Long id) throws IOException {
        Map map = new HashMap();
        map.put("fingerprint", fingerPrint);
        map.put("Authorization", "Bearer" + " " + externalTokenService.getToken());
        SingleOrderResponse response = api.get(orderDetail + id, SingleOrderResponse.class, map);
        if(response.getData() != null) {
            AgentOrder agentOrder = findByOrderId(response.getData().getOrderId());
            response.getData().setOrderStatus(agentOrder.getOrderStatus());
            response.getData().setLocalOrderId(agentOrder.getId());
        }
        return response;
    }

    public SingleOrderResponse OrderDetail(Long id){
        SingleOrderResponse response = api.get(merchCommOrderBaseUrl + id, SingleOrderResponse.class, new HashMap<>());
        if(response.getData() != null) {
            AgentOrder agentOrder = findByOrderId(response.getData().getOrderId());
            response.getData().setOrderStatus(agentOrder.getOrderStatus());
            response.getData().setLocalOrderId(agentOrder.getId());
        }
        return response;
    }


    public OrderHistoryResponse orderHistory(OrderHistoryRequest request) throws IOException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(orderHistory)
                // Add query parameter
                .queryParam("PageNumber", request.getPageNumber())
                .queryParam("PageSize", request.getPageSize());

        Map map = new HashMap();
        map.put("fingerprint", fingerPrint);
        map.put("Authorization", "Bearer" + " " + externalTokenService.getToken());
        OrderHistoryResponse response = api.get(builder.toUriString(), OrderHistoryResponse.class, map);
        return response;
    }

    public MerchBuyResponse merchBuy(MerchBuyRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("fingerprint", fingerPrint);
        map.put("Authorization", "Bearer" + " " + externalTokenService.getToken());
        log.info("Merchant buy url " + merchBuyUrl);
        return api.post(merchBuyUrl, request, MerchBuyResponse.class);
    }

    public AgentOrder findById(long id) {
        return orderRepository.findById(id).orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                "Requested bank id does not exist!"));
    }


    private void saveOrder(PlaceOrder request, CreateOrderResponse response, String paymentMethod) {



        AgentOrder order = AgentOrder.builder()
                .createdDate(new Date())
                .status(response.isStatus())
//                .quantity((long) response.getData().getNoOfItems())
                .orderStatus("PROCESSING")
                .isSentToThirdParty(false)
                .paymentMethod(paymentMethod)
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


    public Page<AgentOrder> findAll(Long orderId, Boolean status, Date createdDate, Long agentId, String userName, PageRequest pageRequest) {
        Page<AgentOrder> agentOrder = orderRepository.findOrders(orderId, status, createdDate, agentId, userName, pageRequest);
        if (agentOrder == null) {
            throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, " No record found !");
        }
        return agentOrder;
    }

    public Page<Map> multiSearch(String searchTerm, Long agentId, String startDate, String endDate, PageRequest pageRequest) {
        Page<Map> objects;

        if (startDate != null && endDate != null) {
            try {
                tryParseDate(startDate);
                tryParseDate(endDate);
                log.info("logging with date");
                objects = orderRepository.singleSearch(searchTerm, agentId,startDate, endDate, pageRequest);
            } catch (ParseException e) {
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Expected date format is yyyy-MM-dd HH:mm:ss");
            }
        } else {
            objects = orderRepository.singleSearch(searchTerm, agentId,pageRequest);
        }

        log.info("No. Of items from search " + objects.stream().count());
        return objects;
    }

    private void tryParseDate(String date) throws ParseException {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
    }

    public AgentOrder findByOrderId(long orderId){
        AgentOrder byOrderId = orderRepository.findByOrderId(orderId);
        if(byOrderId == null ) throw new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION, "Order not found");
        return byOrderId;
    }



    public LocalCompleteOrderResponse localCompleteOrder(LocalCompleteOrderRequest completeOrderRequest) {

        PaymentStatusResponse paymentStatusResponse = paymentService.checkStatus(completeOrderRequest.getPaymentReference());


        if(paymentStatusResponse.getStatus() == null ||
                !paymentStatusResponse.getStatus().equalsIgnoreCase("SUCCESS"))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST,"Cannot process order Payment. Payment incomplete");

        if(paymentStatusResponse.getPaymentDetails().getStatus().equalsIgnoreCase("SUCCESS"))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Payment status conflict");

        log.info("Order id for payment is {}",  paymentStatusResponse.getPaymentDetails().getOrderId());
        AgentOrder agentOrder = findByOrderId(paymentStatusResponse.getPaymentDetails().getOrderId());
        if(agentOrder.getOrderStatus() == null)
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Cannot process order Payment. Order Payment Status not found");
        if(agentOrder.getOrderStatus().equalsIgnoreCase("PAID"))
            throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Cannot process order Payment. Payment already complete for this order");


        paymentStatusResponse.getPaymentDetails().setLinkingReference(completeOrderRequest.getLinkReference());
        paymentStatusResponse.getPaymentDetails().setResponseCode(completeOrderRequest.getCode());
        paymentStatusResponse.getPaymentDetails().setResponseDescription(completeOrderRequest.getMessage());

        agentOrder.setOrderStatus("PAID");
        agentOrder.setOrderDate(new Date());
        agentOrder.setPaidAmount(paymentStatusResponse.getPaymentDetails().getAmount());
        agentOrder.setSentToThirdParty(false);
        agentOrder.setThirdPartyResponseCode(completeOrderRequest.getCode());
        agentOrder.setThirdPartyResponseDesc(completeOrderRequest.getMessage());



        agentOrder.setSuccessPaymentId(paymentStatusResponse.getPaymentDetails().getId());
        log.info("Updating agent order {} ", agentOrder);
        orderRepository.save(agentOrder);
        log.info("Updating payment {} for order {} ", paymentStatusResponse.getPaymentDetails(), agentOrder);
        paymentService.updatePaymentStatus(paymentStatusResponse);

        return LocalCompleteOrderResponse.builder()
                .order(agentOrder)
                .paymentStatus(paymentStatusResponse)
                .build();
    }

    public String paymentMethodString(int paymentMethod) {
        //PayOnDelivery = 1,
        //PayOnline = 2,
        //PayWithWallet = 3,
        //PostPaid = 4,
        //PayWithTransfer = 5
        String paymentMethodString = "";
        switch (paymentMethod) {
            case 0:
                throw new BadRequestException(CustomResponseCode.NOT_FOUND_EXCEPTION,"Error processing payment. Payment method invalid");
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

    public int paymentMethodInt(String paymentMethod) {
        //PayOnDelivery = 1,
        //PayOnline = 2,
        //PayWithWallet = 3,
        //PostPaid = 4,
        //PayWithTransfer = 5
        int paymentMethodString ;
        switch (paymentMethod) {

            case "Pay on Delivery":
                paymentMethodString = 1;
                break;
            case "Pay online":
                paymentMethodString = 2;
                break;
            case "Pay with wallet":
                paymentMethodString = 3;
                break;
            case "Post Paid":
                paymentMethodString = 4;
                break;
            case "Pay with transefer":
                paymentMethodString = 5;
                break;
            default:
                paymentMethodString = 0;
        }
        return paymentMethodString;
    }

    @Async
//    @Scheduled( fixedDelayString = "${order.service.timer}")
    public void completeOrder(){
        log.info("Order Scheduler running");
        Map map = new HashMap();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        List<AgentOrder> paid = orderRepository.findByIsSentToThirdPartyAndOrderStatus(false, "PAID");
        log.info("Number of orders to be sent to third party {} ", paid.size());
        paid.forEach(agentOrder -> {
            Optional<PaymentDetails> paymentDetail = paymentDetailRepository.findById(agentOrder.getSuccessPaymentId());
            log.info("Sending order to third party {} ", agentOrder);
            Payment payment = new Payment();
            CompleteOrderRequest request = new CompleteOrderRequest();
            request.setOrderId(agentOrder.getOrderId());
            payment.setTransactionReference(paymentDetail.get().getPaymentReference());
            payment.setMessage(agentOrder.getThirdPartyResponseDesc());
            payment.setTotal(paymentDetail.get().getApprovedAmount());
            payment.setTransactionId(paymentDetail.get().getLinkingReference());
            payment.setPaymentMethod(paymentMethodInt(agentOrder.getPaymentMethod()));
            payment.setEmail(paymentDetail.get().getEmail());
            request.setPayment(payment);
            log.info(merchCommOrderBaseUrl);
            CompleteOrderResponse post = api.post(merchCommOrderBaseUrl + "ordertransaction", request, CompleteOrderResponse.class, map);
            log.info("Response from complete transaction {}", post);
            if (post.isStatus())
                updateOrder(agentOrder);
        });

    }

    private void updateOrder(AgentOrder agentOrder){
        agentOrder.setSentToThirdParty(true);
        orderRepository.save(agentOrder);
    }
//
//    public List<Map> adminOrder(String agentName, String orderStatus,
//                                String totalAmount, String merchName, int page, int pageSize){
//        log.info("IN orders admin search");
//        return orderRepository.adminSearch(agentName, orderStatus, totalAmount, merchName );
//    }
}
