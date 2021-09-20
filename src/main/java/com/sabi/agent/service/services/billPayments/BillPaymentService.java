package com.sabi.agent.service.services.billPayments;


import com.sabi.agent.core.dto.requestDto.billPayments.AirtimeRequestDto;
import com.sabi.agent.core.dto.requestDto.billPayments.BillCategoryDTO;
import com.sabi.agent.core.dto.requestDto.billPayments.BillerDTO;
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

    public AirtimeRequestDto airtimePayment(Long billerId, String fingerprint, String denomination, String msisdn ){
        AirtimeRequestDto request = AirtimeRequestDto.builder()
                        .billerId(billerId)
                        .denomination(denomination.trim())
                        .msisdn(msisdn.trim())
                        .build();

        String extToken = externalTokenService.getToken().toString();

        Map<String,String> map = new HashMap();
        map.put("fingerprint", fingerprint.trim());
        map.put("Authorization", extToken);
        AirtimeResponseDto response = api.post(airtime, request, AirtimeResponseDto.class, map);
        Airtime airtime = mapper.map(response, Airtime.class);
        airtime = airtimeRepository.save(airtime);
        return mapper.map(airtime, AirtimeRequestDto.class);

    }


    public List<BillCategoryDTO> getBillCategories(String direction, String fingerprint, Integer page, Integer size, String sortBy){
        List<BillCategoryDTO> items = new ArrayList<>();

        BillCategoryDTO request = BillCategoryDTO.builder()
                .direction(direction)
                .fingerprint(fingerprint.trim())
                .page(page)
                .size(size)
                .sortBy(sortBy.trim())
                .build();

        String extToken = externalTokenService.getToken().toString();
        Map<String,String> map = new HashMap();
        map.put("fingerprint", fingerprint.trim());
        map.put("Authorization", extToken);
        try {
            BillCategoryResponseDTO billCategoryResponse = api.get(billCategories, BillCategoryResponseDTO.class, map, request);
            items = billCategoryResponse.getCategorys();
            return items;
        } catch (Exception e){
            logger.info("Error processing request");
            logger.info("message === {} " , e.getMessage());
        }
        return items;
    }

    public List<BillerDTO> getBillCategoryId(Integer billCategoryId, String fingerprint){
        List<BillerDTO> items = new ArrayList<>();

        BillerDTO request = BillerDTO.builder()
                .billCategoryId(billCategoryId)
                .build();

        String extToken = externalTokenService.getToken().toString();
        Map map = new HashMap();
        map.put("fingerprint", fingerprint.trim());
        map.put("Authorization", extToken);
        try {
            BillerResponseDTO billerResponseDTO = api.get(billers, BillerResponseDTO.class, map, billCategoryId);
            items = billerResponseDTO.getBillers();
            return items;
        } catch (Exception e){
            logger.info("Error processing request");
            logger.info("message === {} " , e.getMessage());
        }
        return items;
    }
}
