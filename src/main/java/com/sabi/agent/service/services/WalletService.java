package com.sabi.agent.service.services;

import com.sabi.agent.core.wallet_integration.request.*;
import com.sabi.agent.core.wallet_integration.response.*;
import com.sabi.framework.helpers.API;
import com.sabi.framework.service.ExternalTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WalletService {

    @Value("${wallet.basrurl}")
    private String baseUrl;

    private final API api;
    private final ExternalTokenService tokenService;

    public WalletService(API api, ExternalTokenService tokenService) {
        this.api = api;
        this.tokenService = tokenService;
    }

    private Map<String, String> getHeaders(String fingerPrint){
        Map<String, String> headers = new HashMap<>();
        String token = tokenService.getToken().getToken();
        headers.put("Authorization", "Bearer " + token);
         headers.put("fingerprint", fingerPrint);
         return headers;
    }

    public WalletStatusResponse activateWallet(String fingerPrint){
        //activate wallet does not have a post body
        return  api.post(baseUrl + "/activateWallet", null, WalletStatusResponse.class, getHeaders(fingerPrint));
    }

    public WalletStatusResponse getBalance(String fingerPrint){
        return  api.get(baseUrl + "/balance",  WalletStatusResponse.class, getHeaders(fingerPrint));
    }

    public WalletStatusResponse balanceSync(String fingerPrint){
        return  api.get(baseUrl + "/balanceSync",  WalletStatusResponse.class, getHeaders(fingerPrint));
    }

    public WalletStatusResponse createWallet(String fingerPrint){
        //create wallet does not have a post body
        return  api.post(baseUrl + "/create", null, WalletStatusResponse.class, getHeaders(fingerPrint));
    }

    public WalletResponse debitUser(String fingerPrint, DebitUserRequest debitUserRequest){
        return  api.post(baseUrl + "/debitUser", debitUserRequest, WalletResponse.class, getHeaders(fingerPrint));
    }

    public InitiateTopUpResponse initiateTopUp(String fingerPrint, InitiateTopUpRequest initiateTopUpRequest){
        return  api.post(baseUrl + "/initiateTopUp", initiateTopUpRequest, InitiateTopUpResponse.class, getHeaders(fingerPrint));
    }

    public CompleteTopUpResponse completeTopUp(String fingerPrint, CompleteTopUpRequest completeTopUpRequest){
        return  api.post(baseUrl + "/postTopUpTransaction", completeTopUpRequest, CompleteTopUpResponse.class, getHeaders(fingerPrint));
    }

    public WalletTransactionDetailsResponse walletTransactionDetails(String fingerPrint, WalletTransacitonDetailsRequest walletTransacitonDetailsRequest){
        return  api.post(baseUrl + "/walletTransactionDetails", walletTransacitonDetailsRequest, WalletTransactionDetailsResponse.class, getHeaders(fingerPrint));
    }

    //walletStatus
    public WalletStatusResponse walletStatus(String fingerPrint){
        return  api.get(baseUrl + "/walletStatus",  WalletStatusResponse.class, getHeaders(fingerPrint));
    }

    public WalletResponse walletToBankTransfer(String fingerPrint, WalletToBankTransferRequest walletToBankTransferRequest){
        return  api.post(baseUrl + "/walletToBankTransfer", walletToBankTransferRequest, WalletResponse.class, getHeaders(fingerPrint));
    }

    public WalletResponse walletToWalletTransfer(String fingerPrint, WalletToBankTransferRequest walletToBankTransferRequest){
        return  api.post(baseUrl + "/walletToWalletTransfer", walletToBankTransferRequest, WalletResponse.class, getHeaders(fingerPrint));
    }

}
