package com.sabi.agent.service.services;

import com.sabi.agent.core.models.WalletEntity;
import com.sabi.agent.core.wallet_integration.WalletSignUpDto;
import com.sabi.agent.core.wallet_integration.request.CompleteTopUpRequest;
import com.sabi.agent.core.wallet_integration.request.DebitUserRequest;
import com.sabi.agent.core.wallet_integration.request.InitiateTopUpRequest;
import com.sabi.agent.core.wallet_integration.request.WalletBvnRequest;
import com.sabi.agent.core.wallet_integration.response.*;
import com.sabi.agent.service.repositories.WalletRepository;
import com.sabi.framework.helpers.API;
import com.sabi.framework.service.ExternalTokenService;
import com.sun.jersey.api.uri.UriBuilderImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
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
    private final AgentService agentService;

    public WalletService(API api, ExternalTokenService tokenService, ModelMapper mapper, AgentService agentService) {
        this.api = api;
        this.tokenService = tokenService;
        this.mapper = mapper;
        this.agentService = agentService;
    }

    private Map<String, String> getHeaders(String fingerPrint) {
        Map<String, String> headers = new HashMap<>();
//        String token = tokenService.getToken();
//        headers.put("Authorization", "Bearer " + token);
        headers.put("fingerprint", fingerPrint);
        return headers;
    }


    public ResponseMetaData getBalance(String userId, String fingerPrint) {
        return api.get(baseUrl + "/publicKey/" + publicKey + "/user/" + userId + "/balance", ResponseMetaData.class, getHeaders(fingerPrint));
    }


    public ResponseMetaData createWallet(WalletSignUpDto signUpDto, String fingerPrint) throws NoSuchAlgorithmException {
        //publicKey,firstName,phoneNumber,lastName,email,secretKey
        String dataToHash = publicKey + signUpDto.getFirstName() + signUpDto.getPhoneNumber() + signUpDto.getLastName()
                + signUpDto.getEmail() + privateKey;
        String hased = DigestUtils.sha512Hex(dataToHash);
        signUpDto.setHash(hased);
        signUpDto.setPublicKey(publicKey);
        ResponseMetaData post = api.post(baseUrl + "/create", signUpDto, ResponseMetaData.class, getHeaders(fingerPrint));
        if (post.getCode().equals("00") && post.getData() != null) {
            log.info("Saving Wallet Details: " + post.getData().toString());
            saveWallet(post.getData());
        }
        return post;
    }

    public ResponseMetaData getAllWallets(String fingerPrint, String pageSize, String page) {
        return api.get(baseUrl + "/publicKey/" + publicKey, ResponseMetaData.class, getHeaders(fingerPrint));
    }

    public ResponseMetaData getUserWalletDetails(String fingerPrint, String userId) {
        return api.get(baseUrl + "/userId/" + userId + "/publicKey/" + publicKey, ResponseMetaData.class, getHeaders(fingerPrint));
    }

    public WalletBvnResponse checkBvn(WalletBvnRequest request, String fingerPrint) {
        WalletBvnResponse bvnResponse = api.post(baseUrl + "/publicKey/" + publicKey + "/verifyBVN", request, WalletBvnResponse.class, getHeaders(fingerPrint));
        if (bvnResponse.getData() != null && bvnResponse.getData().isStatus()) {
            log.info("Bvn is verified :: ");
            agentService.agentBvnVerifications(bvnResponse, request.getAgentId());
        }
        return bvnResponse;
    }

    //ignore method
    public WalletResponse debitUser(String fingerPrint, DebitUserRequest debitUserRequest) {
        return api.post(baseUrl + "/debitUser", debitUserRequest, WalletResponse.class, getHeaders(fingerPrint));
    }

    public WalletHistoryResponse walletHistory(String fingerPrint, String endDate, String name, String page, String pageSize, String phoneNumber, String startDate,
                                           String transactionClass, String transactionStatus, String transactionType, String userId, String walletTransactionsSearchTerm,
                                           String walletTransactionsSortCriteria) {
        String uri = "";
        if (endDate != null && !endDate.isEmpty())
            uri += "&endDate=" + endDate;
        if (name != null && !name.isEmpty())
            uri += "&name=" + name;
        if (phoneNumber != null && !phoneNumber.isEmpty())
            uri += "&phoneNumber=" + phoneNumber;
        if (startDate != null && !startDate.isEmpty())
            uri += "&startDate=" + startDate;
        if (transactionClass != null && !transactionClass.isEmpty())
            uri += "&transactionClass=" + transactionClass;
        if (transactionStatus != null && !transactionStatus.isEmpty())
            uri += "&transactionStatus=" + transactionStatus;
        if (transactionType != null && !transactionType.isEmpty())
            uri += "&transactionType=" + transactionType;
        if (walletTransactionsSearchTerm != null && !walletTransactionsSearchTerm.isEmpty())
            uri += "&walletTransactionsSearchTerm=" + walletTransactionsSearchTerm;
        if (walletTransactionsSortCriteria != null && !walletTransactionsSortCriteria.isEmpty())
            uri += "&walletTransactionsSearchTerm" + walletTransactionsSearchTerm;

//        if(!uri.isEmpty())
//        log.info("Final uri for transaction history: " + uri);
        //?page=" + page + "&pageSize=" + pageSize + uri
//        https://api-wallet-dev.spaceso2o.com/api/v3/wallet/publicKey/SAB_uwe8QZiOaGiZXLdWjRjvqp12zK7B82lz/user/2/walletTransactions

        return api.get(baseUrl + "/publicKey/" + publicKey + "/user/" + userId + "/walletTransactions?page=" + page + "&pageSize=" + pageSize + uri, WalletHistoryResponse.class, getHeaders(fingerPrint));
    }

    public InitiateTopUpResponse initiateTopUp(String userId, String fingerPrint, InitiateTopUpRequest initiateTopUpRequest) {
        return api.put(baseUrl + "/publicKey/" + publicKey + "/user/" + userId + "/initiateTopup", initiateTopUpRequest, InitiateTopUpResponse.class, getHeaders(fingerPrint));
    }

    public CompleteTopUpResponse completeTopUp(CompleteTopUpRequest request, String fingerPrint){
        String url = "https://api-wallet-dev.spaceso2o.com/api/v3/wallet/publicKey/"+ publicKey+"/completeTopup";
        return api.put(url, request, CompleteTopUpResponse.class, getHeaders(fingerPrint));
    }

    public void saveWallet(CreateWalletResponse response) {
        WalletEntity data = new WalletEntity();
        data.setWId(response.getId());
        data.setBalance(response.getBalance());
        data.setThirdPartyUserId(response.getThirdPartyUserId());
        data.setFundingLink(response.getFundingLink());
        data.setPhoneNumber(response.getPhoneNumber());
        data.setTotalCommissionEarned(response.getTotalCommissionEarned());
        data.setStatus(response.getStatus());
        data.setWalletType(response.getWalletType());
        data.setWalletUserId(response.getWalletUserId());
        data.setCreatedDate(new Date());
        repository.save(data);
    }
}
