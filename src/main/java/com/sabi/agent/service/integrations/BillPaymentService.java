package com.sabi.agent.service.integrations;


import com.sabi.agent.core.dto.requestDto.billPayments.AirtimeRequestDto;
import com.sabi.agent.core.dto.responseDto.ResponseDto;
import com.sabi.agent.core.dto.responseDto.billPayments.AirtimeResponseDto;
import com.sabi.agent.core.dto.responseDto.billPayments.BillPaymentResponseDto;
import com.sabi.agent.core.models.billPayments.Airtime;
import com.sabi.agent.service.repositories.billPayments.AirtimeRepository;
import com.sabi.framework.helpers.API;
import com.sabi.framework.repositories.ExternalTokenRepository;
import com.sabi.framework.service.ExternalTokenService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("ALL")
@Slf4j
@Service
public class BillPaymentService {

    @Value("${space.airtime}")
    private String airtime;


    @Value("${second.url}")
    private String secondUrl;

    @Value("${space.billcategories}")
    private String billCategories;

    @Value("${space.billers}")
    private String billers;

    @Value(("${space.airtime.authkey}"))
    private String authKey;

    @Value("${bill.category.id}")
    private int billCategoryId;

    @Value("${finger.print}")
    private String fingerprint;

    @Autowired
    ExternalTokenService externalTokenService;

    @Autowired
    private API api;
    @Autowired
    private ExternalTokenRepository externalTokenRepository;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AirtimeRepository airtimeRepository;
    private final ModelMapper mapper;

    public BillPaymentService(AirtimeRepository airtimeRepository, ModelMapper mapper) {
        this.airtimeRepository = airtimeRepository;
        this.mapper = mapper;
    }

    public AirtimeResponseDto airtimePayment(AirtimeRequestDto airtimeRequestDto) {
        log.info("Airtime request dto is " +airtimeRequestDto.toString());
        AirtimeRequestDto request = AirtimeRequestDto.builder()
                .billerId(airtimeRequestDto.getBillerId())
                .denomination(airtimeRequestDto.getDenomination().trim())
                .msisdn(airtimeRequestDto.getMsisdn().trim())
                .requestApp(airtimeRequestDto.getRequestApp().trim())
                .userId(airtimeRequestDto.getUserId().trim())
                .build();

        String extToken = externalTokenService.getToken().toString();

        Map<String, String> map = new HashMap();
        map.put("fingerprint", airtimeRequestDto.getFingerprint().trim());
        map.put("Authorization", "Bearer " + extToken);
        map.put("authKey", authKey);
        AirtimeResponseDto response = api.post(airtime, request, AirtimeResponseDto.class, map);
        Airtime airtime = mapper.map(response, Airtime.class);
        airtime = airtimeRepository.save(airtime);
        return response;
    }

    public Airtime airtimeStatus(String billPurchaseId, String fingerPrint) {

        String extToken = externalTokenService.getToken().toString();

        String url = airtime + "updateStatus/" + billPurchaseId;
        Map<String, String> map = new HashMap();
        map.put("fingerprint", fingerPrint);
        map.put("Authorization", "Bearer " + extToken);
        map.put("authKey", authKey);
        AirtimeResponseDto response = api.put(url, null, AirtimeResponseDto.class, map);
        return mapper.map(response, Airtime.class);
    }

    public BillPaymentResponseDto getBillersPerCategories() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billCategories)
                .queryParam("billCategoryId", billCategoryId);

        Map<String, String> map = new HashMap();
        map.put("fingerprint", fingerprint);
        map.put("Authorization", "Bearer " + externalTokenService.getToken().toString());
        map.put("authKey", authKey);
        return api.get(billCategories + "/" + billCategoryId , BillPaymentResponseDto.class, map);
    }

    public ResponseDto getBillers(String direction, Integer page, Integer size, String sortBy) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billers)
                .queryParam("direction", direction)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy);

        Map map = new HashMap();
        String token = externalTokenService.getToken();
        map.put("fingerprint", fingerprint);
        map.put("Authorization", "Bearer " + token);
        map.put("authKey", authKey);
        log.info("TOKEN .......................... " + token + "...." + fingerprint);
        return api.get(builder.toUriString(), ResponseDto.class, map);
    }

}
