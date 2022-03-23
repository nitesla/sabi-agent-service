package com.sabi.agent.service.services;

import com.sabi.agent.core.dto.requestDto.EnableDisEnableDto;
import com.sabi.agent.core.merchant_integration.request.MerchantSignUpRequest;
import com.sabi.agent.core.merchant_integration.response.*;
import com.sabi.agent.core.models.RegisteredMerchant;
import com.sabi.agent.core.models.agentModel.Agent;
import com.sabi.agent.service.helper.GenericSpecification;
import com.sabi.agent.service.helper.SearchCriteria;
import com.sabi.agent.service.helper.SearchOperation;
import com.sabi.agent.service.helper.Validations;
import com.sabi.agent.service.repositories.MerchantRepository;
import com.sabi.agent.service.repositories.agentRepo.AgentRepository;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.exceptions.ConflictException;
import com.sabi.framework.exceptions.NotFoundException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.models.User;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.service.ExternalTokenService;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.utils.CustomResponseCode;
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
import java.time.LocalDateTime;
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

    private final AgentRepository agentRepository;

    private final UserRepository userRepository;

    @Autowired
    MerchantRepository repository;

    public MerchantService(API api, ExternalTokenService tokenService, ModelMapper mapper, Validations validations, AgentRepository agentRepository, UserRepository userRepository) {
        this.api = api;
        this.tokenService = tokenService;
        this.validations = validations;
        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
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

    public Page<RegisteredMerchant> findMerchant(String agentId, String merchantId, String firstName, String lastName,Boolean isActive,LocalDateTime fromDate, LocalDateTime toDate,PageRequest pageRequest) {
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
        if (isActive!=null)
            genericSpecification.add(new SearchCriteria("isActive", isActive, SearchOperation.EQUAL));
        if (fromDate!=null){
            if (toDate!=null && fromDate.isAfter(toDate))
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST,"fromDate can't be greater than toDate");
            genericSpecification.add(new SearchCriteria("createdDate", fromDate,SearchOperation.GREATER_THAN_EQUAL));
        }
        if (toDate!=null){
            if (fromDate == null)
                throw new BadRequestException(CustomResponseCode.BAD_REQUEST,"'fromDate' must be included along with 'toDate' in the request");
            genericSpecification.add(new SearchCriteria("createdDate", toDate,SearchOperation.LESS_THAN_EQUAL));
        }

       Page<RegisteredMerchant> registeredMerchants= repository.findAll(genericSpecification, pageRequest);
        return getRegisteredMerchantsAndSetAgentName(registeredMerchants);

    }

    private Page<RegisteredMerchant> getRegisteredMerchantsAndSetAgentName(Page<RegisteredMerchant> registeredMerchants) {
        registeredMerchants.getContent().stream().forEach(this::getAndSetMerchantAgentName);
        return registeredMerchants;
    }

    public MerchantDetailResponse merchantDetails(String userId, String fingerPrint){
        return api.get(baseUrl + "/api/users/public/" + userId, MerchantDetailResponse.class, getHeaders(fingerPrint));
    }
    public RegisteredMerchant merchantDetails(Long id) {
        RegisteredMerchant registeredMerchant = repository.findById(id).
                orElseThrow(()->new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,"The requested merchant with this merchantId does not exist"));
        return getAndSetMerchantAgentName(registeredMerchant);
    }

    private RegisteredMerchant getAndSetMerchantAgentName(RegisteredMerchant registeredMerchant) {
        Agent agent =agentRepository.findById(Long.parseLong(registeredMerchant.getAgentId())).orElse(null);
        if(agent!=null){
            User user =userRepository.findById(agent.getUserId()).get();
            registeredMerchant.setAgentName((registeredMerchant.getAgentId()!=null?(user!=null?user.getFirstName()+" "+user.getLastName():null):null));
        }
        return registeredMerchant;
    }

    public Page<RegisteredMerchant> searchMerchant(Long agentId, String searchTerm, PageRequest pageRequest){
        String phoneNumber = null;
        if (!validateName(searchTerm)) phoneNumber = searchTerm;

        if(agentId != null) return repository.searchMerchantsWithAgentId(searchTerm, agentId, phoneNumber, pageRequest);

        return repository.searchMerchantsWithoutAgentId(searchTerm, phoneNumber, pageRequest);
    }

    private boolean validateName(String name) {
        String pattern = "^[a-zA-Z-'][ ]*$";
        return name.matches(pattern);
    }

    public void enableDisEnableState(EnableDisEnableDto request) {
        validations.validateStatus(request.getIsActive());
        User userCurrent = TokenService.getCurrentUserFromSecurityContext();
        RegisteredMerchant merchant = repository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException(CustomResponseCode.NOT_FOUND_EXCEPTION,
                        "Requested Merchant id does not exist!"));
        merchant.setIsActive(request.getIsActive());
        merchant.setUpdatedBy(userCurrent.getId());
        repository.save(merchant);
    }
}
