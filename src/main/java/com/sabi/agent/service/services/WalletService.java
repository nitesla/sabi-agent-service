package com.sabi.agent.service.services;

import com.sabi.agent.core.models.WalletEntity;
import com.sabi.agent.core.wallet_integration.WalletSignUpDto;
import com.sabi.agent.core.wallet_integration.request.DebitUserRequest;
import com.sabi.agent.core.wallet_integration.request.InitiateTopUpRequest;
import com.sabi.agent.core.wallet_integration.request.WalletBvnRequest;
import com.sabi.agent.core.wallet_integration.response.*;
import com.sabi.agent.service.repositories.WalletRepository;
import com.sabi.framework.helpers.API;
import com.sabi.framework.helpers.Encryptions;
import com.sabi.framework.service.ExternalTokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WalletService {

    @Value("${wallet.basrurl}")
    private String baseUrl;

    @Value("${wallet.publickey}")
    private String publicKey;

    @Value("${wallet.secretkey}")
    private String privateKey;

    @Autowired
    private WalletRepository repository;

    private final API api;
    private final ExternalTokenService tokenService;
    private final ModelMapper mapper;

    public WalletService(API api, ExternalTokenService tokenService, ModelMapper mapper) {
        this.api = api;
        this.tokenService = tokenService;
        this.mapper = mapper;
    }

    private Map<String, String> getHeaders(String fingerPrint){
        Map<String, String> headers = new HashMap<>();
        String token = tokenService.getToken();
//        headers.put("Authorization", "Bearer " + token);
         headers.put("fingerprint", fingerPrint);
         return headers;
    }



    public ResponseMetaData getBalance(String userId, String fingerPrint){
        return  api.get(baseUrl + "/publicKey/" +publicKey + "/user/"+userId + "/balance",  ResponseMetaData.class, getHeaders(fingerPrint));
    }


    public ResponseMetaData createWallet(WalletSignUpDto signUpDto, String fingerPrint) throws NoSuchAlgorithmException {
        //publicKey,firstName,phoneNumber,lastName,email,secretKey
        String dataToHash = publicKey + signUpDto.getFirstName() + signUpDto.getPhoneNumber() + signUpDto.getLastName()
                + signUpDto.getEmail() + privateKey;
        String hased = DigestUtils.sha512Hex(dataToHash);
        log.info("?????????????????????????  ? " +hased);
        signUpDto.setHash(hased);
        signUpDto.setPublicKey(publicKey);
        log.info("))))))))))))))))))))))        "+ signUpDto.toString() + "  " + privateKey);
        ResponseMetaData post = api.post(baseUrl + "/create", signUpDto, ResponseMetaData.class, getHeaders(fingerPrint));
        if (post.getCode().equals("00")) saveWallet(post.getData());
        return post;
    }

    public ResponseMetaData getAllWallets(String fingerPrint, String pageSize, String page){
        return api.get(baseUrl+ "/publicKey/" + publicKey, ResponseMetaData.class, getHeaders(fingerPrint));
    }

    public ResponseMetaData getUserWalletDetails(String fingerPrint, String userId){
        return api.get(baseUrl + "/userId/" +userId+"/publicKey/" +publicKey, ResponseMetaData.class, getHeaders(fingerPrint));
    }

    public WalletBvnResponse checkBvn(WalletBvnRequest request, String fingerPrint){
        return api.post(baseUrl + "/publicKey/"+publicKey+"/verifyBVN", request,WalletBvnResponse.class, getHeaders(fingerPrint));
    }

    //ignore method
    public WalletResponse debitUser(String fingerPrint, DebitUserRequest debitUserRequest){
        return  api.post(baseUrl + "/debitUser", debitUserRequest, WalletResponse.class, getHeaders(fingerPrint));
    }

    public InitiateTopUpResponse initiateTopUp(String userId, String fingerPrint, InitiateTopUpRequest initiateTopUpRequest){
        return  api.put(baseUrl + "/publicKey/"+publicKey + "/user/"+ userId+"/initiateTopup", initiateTopUpRequest, InitiateTopUpResponse.class, getHeaders(fingerPrint));
    }

    public void saveWallet(CreateWalletResponse response){
        WalletEntity data = mapper.map(response, WalletEntity.class);
        data.setCreatedDate(new Date());
        repository.save(data);
    }
}
