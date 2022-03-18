package com.sabi.agent.service.services;

import com.sabi.agent.core.merchant_integration.request.MerchantSignUpRequest;
import com.sabi.agent.core.merchant_integration.response.*;
import com.sabi.agent.core.models.RegisteredMerchant;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.MerchantRepository;
import com.sabi.framework.helpers.API;
import com.sabi.framework.models.User;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MerchantService {

    @Value("${merchant.basrurl}")
    private String baseUrl;

    private final API api;
    private final ExternalTokenService tokenService;
    private final Validations validations;

    @Value("${merchant.signup}")
    private String merchantSignUpUrl;

    @Autowired
    MerchantRepository repository;

    public MerchantService(API api, ExternalTokenService tokenService, ModelMapper mapper, Validations validations) {
        this.api = api;
        this.tokenService = tokenService;
        this.validations = validations;
    }

    private Map<String, String> getHeaders(String fingerPrint) {
        Map<String, String> headers = new HashMap<>();
        String token = tokenService.getToken();
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" + token);
//        headers.put("Authorization", "Bearer " + token);
        headers.put("fingerprint", fingerPrint);
        return headers;
    }

    public MerchantSignUpResponse createMerchant(MerchantSignUpRequest signUpRequest, String fingerPrint) {
        validations.validateCreateMerchant(signUpRequest);
        signUpRequest.setCreatedDate(String.valueOf(LocalDateTime.now()));
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        String createdBy = userCurrent.getFirstName() + " " + userCurrent.getLastName();
        signUpRequest.setCreatedBy(createdBy);
        MerchantSignUpResponse signUpResponse = api.post(merchantSignUpUrl,
                signUpRequest, MerchantSignUpResponse.class, getHeaders(fingerPrint));
        if (signUpResponse.getId() != null){
             RegisteredMerchant registeredMerchant = saveMerchant(signUpResponse, signUpRequest);
            signUpResponse.setLocalId(registeredMerchant.getId());
            signUpResponse.setCountry(signUpRequest.getCountryCode());
            signUpResponse.setBusinessName(signUpRequest.getBusinessName());
            signUpResponse.setState(signUpRequest.getState());
            signUpResponse.setLga(signUpRequest.getLga());
        }
        return signUpResponse;
    }

    public RegisteredMerchant saveMerchant(MerchantSignUpResponse signUpResponse, MerchantSignUpRequest signUpRequest) {
        RegisteredMerchant registeredMerchant = new RegisteredMerchant();
        registeredMerchant.setAgentId(signUpRequest.getAgentId());
        registeredMerchant.setEmail(signUpResponse.getEmail());
        registeredMerchant.setFirstName(signUpResponse.getFirstName());
        registeredMerchant.setAddress(signUpResponse.getAddress());
        registeredMerchant.setIsActive(true);
        registeredMerchant.setLastName(signUpRequest.getLastName());
        registeredMerchant.setBusinessName(signUpRequest.getBusinessName());
        registeredMerchant.setPhoneNumber(signUpResponse.getPhoneNumber());
        registeredMerchant.setMerchantId(signUpResponse.getId());
        registeredMerchant.setLga(signUpRequest.getLga());
        registeredMerchant.setState(signUpRequest.getState());
        registeredMerchant.setCountry(signUpRequest.getCountryCode());
        return repository.save(registeredMerchant);
    }

    public MerchantWithActivityResponse getMerchantWithActivity(int page, int size, String fingerPrint) {
        return api.get("/api/users/getMerchantsWithActivity/v2?page=" + page + "&size=" + size, MerchantWithActivityResponse.class, getHeaders(fingerPrint));
    }

    public MerchantOtpResponse sendOtp(String fingerPrint, String msisdn) throws UnsupportedEncodingException {
        if (msisdn.startsWith("0")) {
            msisdn = "+234" + msisdn.substring(1);
        }
        if (msisdn.startsWith("234")) {
            msisdn = "+" + msisdn;
        }
//        if(!msisdn.startsWith("+234")) throw new BadRequestException(CustomResponseCode.BAD_REQUEST, "Phone Number must Start with 234");
        String url = baseUrl + "/api/otp/send/mobile?msisdn=" + encodeValue(msisdn);
        log.info("Merchant phone  is " + msisdn);
        return api.get(url, MerchantOtpResponse.class, getHeaders(fingerPrint));
    }

    public MerchantOtpValidationResponse validateOtp(String fingerPrint, String userId, String otp) {
        return api.post(baseUrl + "/api/otp/check?code=" + otp + "&userId=" + userId, null, MerchantOtpValidationResponse.class, getHeaders(fingerPrint));
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {

        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    public Page<RegisteredMerchant> findMerchant(String agentId, String merchantId, String firstName, String lastName,PageRequest pageRequest) {
        GenericSpecification<RegisteredMerchant> genericSpecification = new GenericSpecification<RegisteredMerchant>();

        if (agentId != null && !agentId.isEmpty()) {
            genericSpecification.add(new SearchCriteria("agentId", agentId, SearchOperation.MATCH));
        }
        if (merchantId != null && !merchantId.isEmpty()) {
            genericSpecification.add(new SearchCriteria("merchantId", merchantId, SearchOperation.EQUAL));
        }
        if(firstName != null && !firstName.isEmpty())
            genericSpecification.add(new SearchCriteria("firstName", firstName, SearchOperation.MATCH));
        if(lastName !=null && !lastName.isEmpty())
            genericSpecification.add(new SearchCriteria("lastName", lastName, SearchOperation.MATCH));
        return repository.findAll(genericSpecification, pageRequest);
    }

    public MerchantDetailResponse merchantDetails(String userId, String fingerPrint){
        return api.get(baseUrl + "/api/users/public/" + userId, MerchantDetailResponse.class, getHeaders(fingerPrint));
    }

    public Page<RegisteredMerchant> searchMerchant(Long agentId, String searchTerm, PageRequest pageRequest){
        String phoneNumber = null;
        if (!validateName(searchTerm)) phoneNumber = searchTerm;

        if(agentId != null) return repository.searchMerchants(searchTerm, agentId, phoneNumber, pageRequest);

        return repository.searchMerchantsWithoutAgentId(searchTerm, phoneNumber, pageRequest);
    }

    private boolean validateName(String name) {
        String pattern = "^[a-zA-Z-'][ ]*$";
        return name.matches(pattern);
    }
}
