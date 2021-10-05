package com.sabi.agent.service.services;

import com.sabi.agent.core.merchant_integration.request.MerchantSignUpRequest;
import com.sabi.agent.core.merchant_integration.response.MerchantSignUpResponse;
import com.sabi.agent.core.merchant_integration.response.MerchantWithActivityResponse;
import com.sabi.framework.helpers.API;
import com.sabi.framework.service.ExternalTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MerchantService {

    @Value("${merchant.basrurl}")
    private String baseUrl;

    private final API api;
    private final ExternalTokenService tokenService;

    public MerchantService(API api, ExternalTokenService tokenService) {
        this.api = api;
        this.tokenService = tokenService;
    }

    private Map<String, String> getHeaders(String fingerPrint){
        Map<String, String> headers = new HashMap<>();
        String token = tokenService.getToken();
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" +token);
        headers.put("Authorization", "Bearer " + token);
        headers.put("fingerprint", fingerPrint);
        return headers;
    }

    public MerchantSignUpResponse createMerchant(MerchantSignUpRequest signUpRequest, String fingerPrint){
        MerchantSignUpResponse signUpResponse = api.post(baseUrl + "/api/v3/completeSignup", signUpRequest, MerchantSignUpResponse.class, getHeaders(fingerPrint));
        return signUpResponse;
    }

    public MerchantWithActivityResponse getMerchantWithActivity(int page, int size, String fingerPrint){
        return api.get("/api/users/getMerchantsWithActivity/v2?page="+page+"&size=" +size, MerchantWithActivityResponse.class, getHeaders(fingerPrint));
    }
}
