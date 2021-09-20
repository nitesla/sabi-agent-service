package com.sabi.agent.service.services;


import com.sabi.agent.core.dto.requestDto.NotificationRequestDto;
import com.sabi.agent.core.dto.responseDto.NotificationResponseDto;
import com.sabi.agent.core.models.notifications.Notification;
import com.sabi.agent.core.models.notifications.Recipient;
import com.sabi.agent.service.repositories.NotificationRepository;
import com.sabi.framework.helpers.API;
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
public class NotificationService {

    @Value("${space.notification.url}")
    private String multipleNotification;

    @Value("${authKey.notification}")
    private String authKey;

    @Value("${phoneNo.notification}")
    private String phoneNo;


    @Autowired
    ExternalTokenService externalTokenService;

    @Autowired
    private API api;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private NotificationRepository notificationRepository;
    private final ModelMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, ModelMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    public NotificationRequestDto emailNotificationRequest (NotificationRequestDto notification, String fingerprint){

        Recipient recipient = Recipient.builder()
                            .email(notification.getEmail1())
                            .phoneNo(phoneNo)
                            .build();
        Notification request = Notification.builder()
                        .email(true)
                        .inApp(false)
                        .message(notification.getMessage())
                        .recipients(recipient)
                        .sms(false)
                        .title(notification.getTitle())
                        .build();
        String extToken = externalTokenService.getToken().toString();
        Map<String,String> map = new HashMap();
        map.put("fingerprint", fingerprint.trim());
        map.put("auth-key", authKey.trim());
//        map.put("authorization", extToken);
        NotificationResponseDto response = api.post(multipleNotification, request, NotificationResponseDto.class, map);
        Notification notification1 = mapper.map(response, Notification.class);
        notification1 = notificationRepository.save(notification1);
        return mapper.map(notification, NotificationRequestDto.class);

    }

    public Notification smsNotificationRequest (String message, String email, String title, String fingerprint){

        Recipient recipient = Recipient.builder()
                .email(email)
                .phoneNo(phoneNo)
                .build();
        Notification request = Notification.builder()
                .email(false)
                .inApp(false)
                .message(message)
                .recipients(recipient)
                .sms(true)
                .title(title)
                .build();
        String extToken = externalTokenService.getToken().toString();
        Map<String,String> map = new HashMap();
        map.put("fingerprint", fingerprint.trim());
        map.put("auth-key", authKey.trim());
        map.put("Authorization", extToken);
        NotificationResponseDto response = api.post(multipleNotification, request, NotificationResponseDto.class, map);
        Notification notification = mapper.map(response, Notification.class);
        notification = notificationRepository.save(notification);
        return mapper.map(notification, Notification.class);

    }


}
