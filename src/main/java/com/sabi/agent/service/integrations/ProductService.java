package com.sabi.agent.service.integrations;


import com.sabi.agent.core.integrations.request.AllProductsRequest;
import com.sabi.agent.core.integrations.request.SingleProductRequest;
import com.sabi.agent.core.integrations.response.MerchantProductCategory;
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
    @Value("${merchant.product.category}")
    private String merchantProductCategoryURL;
    @Value("${product.categoryById}")
    private String productCategoryByCategoryId;



    public SingleProductResponse productDetail (SingleProductRequest request) throws IOException {

        Map map=new HashMap();
        map.put("fingerprint",fingerPrint);
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

    public MerchantProductCategory[] getMerchantProductCategory () throws IOException {
        Map map = new HashMap();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        return api.get(merchantProductCategoryURL, MerchantProductCategory[].class, map);
    }

    public AllProductResponse getProductById(String categoryId, String direction, int page, int pageSize, String sortBy, String state) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(productCategoryByCategoryId)
                .path(categoryId)
                .queryParam("direction", direction)
                .queryParam("page", page)
                .queryParam("pageSize", pageSize)
                .queryParam("sortBy", sortBy)
                .queryParam("state", state);
        Map map = new HashMap();
        map.put("fingerprint",fingerPrint);
        map.put("Authorization","Bearer"+ " " +externalTokenService.getToken());
        return api.get(builder.toUriString(), AllProductResponse.class, map);
    }

}
