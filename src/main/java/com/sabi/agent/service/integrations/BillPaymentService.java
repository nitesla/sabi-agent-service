package com.sabi.agent.service.integrations;


import com.sabi.agent.core.dto.requestDto.billPayments.AirtimeRequestDto;
import com.sabi.agent.core.dto.responseDto.ResponseDto;
import com.sabi.agent.core.dto.responseDto.billPayments.AirtimeResponseDto;
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

    @Value("${space.airtime.url}")
    private String airtime;

    @Value("${second.url}")
    private String secondUrl;

    @Value("${space.billcategories.url}")
    private String billCategories;

    @Value("${space.billers.url}")
    private String billers;

    @Value(("${space.airtime.authkey}"))
    private String authKey;

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
                .userId(airtimeRequestDto.getUserId())
                .requestApp(airtimeRequestDto.getRequestApp())
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


    public ResponseDto getBillCategories(String direction, String fingerprint, Integer page, Integer size, String sortBy) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billCategories)
                .queryParam("direction", direction)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy);

        Map<String, String> map = new HashMap();
        map.put("fingerprint", fingerprint);
        map.put("Authorization", "Bearer " + externalTokenService.getToken().toString());
        ResponseDto items = api.get(builder.toUriString(), ResponseDto.class, map);
        return items;
    }

    public ResponseDto getBillCategoryId(int billCategoryId, String fingerprint) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billers)
                .queryParam("billCategoryId", billCategoryId);

        Map map = new HashMap();
        String url = billers + "/" + billCategoryId;
        String token = externalTokenService.getToken();
        map.put("fingerprint", fingerprint.trim());
        map.put("Authorization", "Bearer " + token);
        log.info("bill payment uri ========" + url + "     " + billCategoryId);
        log.info("TOKEN .......................... " + token + "...." + fingerprint);
        ResponseDto items = api.get(url, ResponseDto.class, map);

        log.info(items.getData().toString());
        return items;
    }
}
