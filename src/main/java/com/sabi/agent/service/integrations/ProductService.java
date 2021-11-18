package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.request.AllProductsRequest;
import com.sabi.agent.core.integrations.request.SingleProductRequest;
import com.sabi.agent.core.integrations.response.SingleProductResponse;
import com.sabi.agent.core.integrations.response.product.AllProductResponse;
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
public class ProductService {


    @Autowired
    private API api;
    @Autowired
    private ExternalTokenService externalTokenService;
    @Value("${product.detail.url}")
    private String productDetail;
    @Value("${allproducts.url}")
    private String allProductDetail;
    @Value("${finger.print}")
    private String fingerPrint;



    public SingleProductResponse productDetail (SingleProductRequest request) throws IOException {

        Map map=new HashMap();
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        SingleProductResponse response = api.get(productDetail + request.getId(), SingleProductResponse.class,map);
        return response;
    }


    public AllProductResponse allProductDetail (AllProductsRequest request) throws IOException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(allProductDetail)
                // Add query parameter
                .queryParam("direction", request.getDirection())
                .queryParam("page",request.getPageNumber())
                .queryParam("pageSize",request.getPageSize())
                .queryParam("searchString",request.getSearchString())
                .queryParam("sortBy",request.getSortBy())
                .queryParam("State",request.getState());


        Map map=new HashMap();
        map.put("fingerprint",fingerPrint);
        AllProductResponse response = api.get(builder.toUriString(), AllProductResponse.class,map);
        return response;
    }

}
