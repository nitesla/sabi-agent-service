package com.sabi.agent.service.services.billPayments;


import com.sabi.agent.core.dto.requestDto.billPayments.AirtimeRequestDto;
import com.sabi.agent.core.dto.requestDto.billPayments.BillCategoryRequestDTO;
import com.sabi.agent.core.dto.responseDto.billPayments.AirtimeResponseDto;
import com.sabi.agent.core.dto.responseDto.billPayments.BillCategoryResponseDTO;
import com.sabi.agent.core.dto.responseDto.billPayments.BillerResponseDTO;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("ALL")
@Slf4j
@Service
public class BillPaymentService {

    @Value("${space.airtime.url}")
    private String airtime;

    @Value("${space.billcategories.url}")
    private String billCategories;

    @Value("${space.billers.url}")
    private String billers;

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

    public AirtimeRequestDto airtimePayment(AirtimeRequestDto airtimeRequestDto ){
        AirtimeRequestDto request = AirtimeRequestDto.builder()
                        .billerId(airtimeRequestDto.getBillerId())
                        .denomination(airtimeRequestDto.getDenomination().trim())
                        .msisdn(airtimeRequestDto.getMsisdn().trim())
                        .build();

        String extToken = externalTokenService.getToken().toString();

        Map<String,String> map = new HashMap();
        map.put("fingerprint", airtimeRequestDto.getFingerprint().trim());
        map.put("Authorization", extToken);
        AirtimeResponseDto response = api.post(airtime, request, AirtimeResponseDto.class, map);
        Airtime airtime = mapper.map(response, Airtime.class);
        airtime = airtimeRepository.save(airtime);
        return mapper.map(airtime, AirtimeRequestDto.class);

    }


    public List<BillCategoryResponseDTO> getBillCategories(BillCategoryRequestDTO request){

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billCategories)
                .queryParam("direction",request.getDirection())
                .queryParam("page",request.getPage())
                .queryParam("size",request.getSize())
                .queryParam("sortBy",request.getSortBy());

        List<BillCategoryResponseDTO> items = new ArrayList<>();
        Map<String,String> map = new HashMap();
        map.put("fingerprint", request.getFingerprint());
        map.put("Authorization", externalTokenService.getToken().toString());
        try {
            BillCategoryResponseDTO billCategoryResponse = api.get(builder.toUriString(), BillCategoryResponseDTO.class, map);
            items = billCategoryResponse.getCategorys();
        } catch (Exception e){
            logger.info("Error processing request");
            logger.info("message === {} " , e.getMessage());
        }
        return items;
    }

    public List<BillerResponseDTO> getBillCategoryId(Integer billCategoryId, String fingerprint){

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billers)
                .queryParam("billCategoryId",billCategoryId);


        List<BillerResponseDTO> items = new ArrayList<>();

        Map map = new HashMap();
        map.put("fingerprint", fingerprint.trim());
        map.put("Authorization", externalTokenService.getToken());
        try {
            BillerResponseDTO billerResponseDTO = api.get(builder.toUriString(), BillerResponseDTO.class, map);
            items = billerResponseDTO.getBillers();
        } catch (Exception e){
            logger.info("Error processing request");
            logger.info("message === {} " , e.getMessage());
        }
        return items;
    }
}
