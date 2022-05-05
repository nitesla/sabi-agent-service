package com.sabi.agent.service.services;

import com.sabi.agent.core.dto.requestDto.GlobalAdminAuthRequestDto;
import com.sabi.agent.core.dto.responseDto.AuthenticateUserResponseDto;
import com.sabi.agent.core.dto.responseDto.GlobalAdminAuthResponse;
import com.sabi.framework.exceptions.BadRequestException;
import com.sabi.framework.helpers.API;
import com.sabi.framework.models.User;
import com.sabi.framework.notification.requestDto.NotificationRequestDto;
import com.sabi.framework.notification.requestDto.RecipientRequest;
import com.sabi.framework.repositories.UserRepository;
import com.sabi.framework.security.AuthenticationWithToken;
import com.sabi.framework.service.NotificationService;
import com.sabi.framework.service.PermissionService;
import com.sabi.framework.service.TokenService;
import com.sabi.framework.service.UserService;
import com.sabi.framework.utils.Constants;
import com.sabi.framework.utils.CustomResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class GlobalAdminAuthService {

    private final API api;

    private final UserRepository userRepository;

    private final TokenService tokenService;

    private final UserService userService;

    private final PermissionService permissionService;

    private final PasswordEncoder passwordEncoder;

    @Value("${global_url}")
    private String globalUrl;

    @Value("${loginUrl}")
    private String loginUrl;

    @Value("{agent.code}")
    private String agentCode;

    private final NotificationService notificationService;

    public GlobalAdminAuthService(API api, UserRepository userRepository, TokenService tokenService, UserService userService, PermissionService permissionService, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.api = api;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.userService = userService;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public GlobalAdminAuthResponse authenticateUser(GlobalAdminAuthRequestDto requestDto) {
        requestDto.setApplicationCode(agentCode);
        String accessToken = null;
        GlobalAdminAuthResponse globalAdminAuthResponse = new  GlobalAdminAuthResponse();
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        AuthenticateUserResponseDto authResponse = authenticateUser(
                requestDto.getApplicationCode(),requestDto.getAuthKey(),
                requestDto.getUserId());
        log.info("Response for Global Admin ::::::::::::::::::::::::: {} ", authResponse);
        if (!authResponse .getCode().equals("200") ){
            log.info("404 Error logged ::::::::::::::::::::::");
            throw new BadRequestException(CustomResponseCode.UNAUTHORIZED, "Unauthorized");
        }else{
            User user = confirmUser(authResponse);
            if (user != null){
                log.info("User should be logged in :::::::::::::::::: ");
                accessToken = "Bearer" +" "+this.tokenService.generateNewToken();
            } else {
                Random rand = new Random();
                User userTosave = new User();
                String passwrd = String.valueOf(rand.nextInt(9999999));
                userTosave.setFirstName(authResponse.getData().getFirstName());
                userTosave.setLastName(authResponse.getData().getLastName());
                userTosave.setPassword(passwordEncoder.encode(passwrd));
                userTosave.setPhone(authResponse.getData().getPhone());
                userTosave.setEmail(authResponse.getData().getUsername());
                userTosave.setUsername(authResponse.getData().getUsername());
                userTosave.setLoginAttempts(0);
                userTosave.setUserCategory(Constants.GLOBAL_ADMIN);
                userTosave.setIsActive(true);
                userTosave.setPasswordChangedOn(LocalDateTime.now());
                userTosave.setCreatedBy(0L);
                userTosave.setCreatedDate(LocalDateTime.now());
                userTosave.setUpdatedDate(LocalDateTime.now());
                userRepository.save(userTosave);
                String accessList = permissionService.getPermissionsByUserId(user.getId());
                AuthenticationWithToken authWithToken = new AuthenticationWithToken(user, null,
                        AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER,"+accessList));
                accessToken = "Bearer" +" "+this.tokenService.generateNewToken();
                authWithToken.setToken(accessToken);
                tokenService.store(accessToken, authWithToken);
                SecurityContextHolder.getContext().setAuthentication(authWithToken);
                userService.updateLogin(user.getId());
//                         authenticateLogin(user.getUsername(),passwrd);
                notificationRequestDto.setMessage("User Default password is : " + " " + passwrd);
                log.info("New User Password ::::::::::::::: " + passwrd);
                List<RecipientRequest> recipient = new ArrayList<>();
                recipient.add(RecipientRequest.builder()
                        .email(user.getEmail())
                        .build());
                notificationRequestDto.setRecipient(recipient);
                notificationService.emailNotificationRequest(notificationRequestDto);
            }
            String accessList = permissionService.getPermissionsByUserId(user.getId());
            AuthenticationWithToken authWithToken = new AuthenticationWithToken(user, null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER,"+accessList));
            accessToken = "Bearer" +" "+this.tokenService.generateNewToken();
            authWithToken.setToken(accessToken);
            tokenService.store(accessToken, authWithToken);
            SecurityContextHolder.getContext().setAuthentication(authWithToken);
            userService.updateLogin(user.getId());
            globalAdminAuthResponse.setToken(accessToken);
            log.info("User Access token ::::::::::::::::::::: " + globalAdminAuthResponse.getToken());

        }
        return globalAdminAuthResponse;
    }


    public AuthenticateUserResponseDto authenticateUser(
            String applicationCode, String authKey, String userId) {
        AuthenticateUserResponseDto response = api.get(
                globalUrl + "appinfo/authkey"+"?"+"applicationCode="+applicationCode+"&"+"authKey="+
                        authKey+"&"+"userId="+userId,
                AuthenticateUserResponseDto.class,getHeaders());
        log.info("Response of associated account ::::::::::::::: " + response);
        return response;
    }

    private User confirmUser(AuthenticateUserResponseDto responseDto){
        User user = userRepository.findByUsername(responseDto.getData().getUsername());
        return user;
    }

//    public String authenticateLogin(String userName, String password){
//        Map<Object,String>request = new HashMap<>();
//        request.put("username",userName);
//        request.put("password",password);
//        AccessTokenResponse response = api.post("spinel-sabiagent-api-mainbranch-844333341.eu-west-2.elb.amazonaws.com/supplier/api/v1/authenticate/login", request,
//                AccessTokenResponse.class,getHeaders());
//        String access = response.getAccessToken();
//        return access;
//    }

    private Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap();
        headers.put("accept", "application/json");
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
