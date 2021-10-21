//package com.sabi.agent.service.services;
//
//import com.sabi.agent.core.wallet_integration.request.DebitUserRequest;
//import com.sabi.agent.core.wallet_integration.response.WalletResponse;
//import com.sabi.framework.helpers.API;
//import com.sabi.framework.service.ExternalTokenService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static org.junit.Assert.assertSame;
//import static org.mockito.Mockito.*;
//
//@ContextConfiguration(classes = {WalletService.class, String.class})
//@RunWith(SpringRunner.class)
//public class WalletServiceTest {
//    @MockBean
//    private API aPI;
//
//    @MockBean
//    private ExternalTokenService externalTokenService;
//
//    @Autowired
//    private WalletService walletService;
//
//    @Test
//    public void testDebitUser() {
////        SpaceResponse spaceResponse = new SpaceResponse();
////        spaceResponse.setToken("ABC123");
//        String spaceResponse = "ABC123";
//        when(externalTokenService.getToken()).thenReturn(spaceResponse);
//        WalletResponse walletResponse = new WalletResponse();
//        when(aPI.post((String) any(), (Object) any(), (Class<Object>) any(), (java.util.Map<String, String>) any()))
//                .thenReturn(walletResponse);
//        assertSame(walletResponse,
//                walletService.debitUser("b6:03:0e:39:97:9e:d0:e7:24:ce:a3:77:3e:01:42:09", new DebitUserRequest()));
//        verify(externalTokenService).getToken();
//        verify(aPI).post((String) any(), (Object) any(), (Class<Object>) any(), (java.util.Map<String, String>) any());
//    }
//}
//
