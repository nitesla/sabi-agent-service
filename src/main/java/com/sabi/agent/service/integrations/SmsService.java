package com.sabi.agent.service.integrations;


import com.sabi.agent.core.dto.requestDto.SmsRequestDto;
import com.sabi.agent.core.dto.responseDto.SmsResponseDto;
import com.sabi.agent.core.models.Sms;
import com.sabi.agent.service.repositories.SmsRepository;
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

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("ALL")
@Slf4j
@Service
public class SmsService {

    @Value("${space.sms.url}")
    private String sms;


    @Autowired
    private API api;

    @Autowired
    ExternalTokenService externalTokenService;

    @Autowired
    private ExternalTokenRepository externalTokenRepository;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SmsRepository smsRepository;
    private final ModelMapper mapper;

    public SmsService(SmsRepository smsRepository, ModelMapper mapper) {
        this.smsRepository = smsRepository;
        this.mapper = mapper;
    }

    public SmsRequestDto testSms(SmsRequestDto smsRequestDto){
        SmsRequestDto request = SmsRequestDto.builder()
                        .message(smsRequestDto.getMessage())
                        .phoneNumber(smsRequestDto.getPhoneNumber().trim())
                        .build();
        String extToken = externalTokenService.getToken();
        Map map = new HashMap();
        map.put("fingerprint", smsRequestDto.getFingerprint().trim());
        map.put("Authorization", "bearer"+ " " +extToken);
        SmsResponseDto response = api.post(sms, request, SmsResponseDto.class, map);
        Sms sms = mapper.map(response, Sms.class);
        sms = smsRepository.save(sms);
        return mapper.map(sms, SmsRequestDto.class);

    }


}
