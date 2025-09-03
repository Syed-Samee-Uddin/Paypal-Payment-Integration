package com.hulkhiretech.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.constant.PaypalStatusEnum;
import com.hulkhiretech.payments.constant.TxnStatusEnum;
import com.hulkhiretech.payments.dao.interfaces.TransactionDAO;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypalprovider.PPOrder;
import com.hulkhiretech.payments.service.helper.PPCaptureOrderHelper;
import com.hulkhiretech.payments.service.helper.PPGetOrderHelper;
//import com.hulkhiretech.payments.utils.JsonUtils;

@ExtendWith(MockitoExtension.class)
class PaypalProviderHandlerAIGeneratedTest {

    @Mock
    private HttpServiceEngine httpServiceEngine;

//    @Mock
//    private JsonUtils jsonUtils;

    @Mock
    private PPGetOrderHelper ppGetOrderHelper;

    @Mock
    private PPCaptureOrderHelper ppCaptureOrderHelper;

    @Mock
    private TransactionDAO transactionDAO;

    @InjectMocks
    private PaypalProviderHandler paypalProviderHandler;

    private TransactionDTO txn;

    @BeforeEach
    void setUp() {
        txn = new TransactionDTO();
        txn.setRetryCount(0);
        txn.setTxnStatus(TxnStatusEnum.PENDING.getName());
    }

    @Test
    void testCompletedStatus() {
        PPOrder successObj = new PPOrder();
        successObj.setPaypalStatus(PaypalStatusEnum.COMPLETED.getName());

        when(ppGetOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(httpServiceEngine.makeHttpCall(any())).thenReturn(ResponseEntity.ok("response"));
        when(ppGetOrderHelper.processGetOrderResponse(any())).thenReturn(successObj);

        paypalProviderHandler.reconTransaction(txn);

        assertEquals(TxnStatusEnum.SUCCESS.getName(), txn.getTxnStatus());
        assertEquals(1, txn.getRetryCount());
        verify(transactionDAO).updateTransactionForRecon(txn);
    }

    @Test
    void testPayerActionRequiredStatus() {
        PPOrder successObj = new PPOrder();
        successObj.setPaypalStatus(PaypalStatusEnum.PAYER_ACTION_REQUIRED.getName());

        when(ppGetOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(httpServiceEngine.makeHttpCall(any())).thenReturn(ResponseEntity.ok("response"));
        when(ppGetOrderHelper.processGetOrderResponse(any())).thenReturn(successObj);

        paypalProviderHandler.reconTransaction(txn);

        assertEquals(TxnStatusEnum.PENDING.getName(), txn.getTxnStatus());
        assertEquals(1, txn.getRetryCount());
        verify(transactionDAO).updateTransactionForRecon(txn);
    }

    @Test
    void testApprovedStatusCaptureSuccess() {
        PPOrder getOrderResponse = new PPOrder();
        getOrderResponse.setPaypalStatus(PaypalStatusEnum.APPROVED.getName());

        PPOrder captureResponse = new PPOrder();
        captureResponse.setPaypalStatus(PaypalStatusEnum.COMPLETED.getName());

        when(ppGetOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(httpServiceEngine.makeHttpCall(any())).thenReturn(ResponseEntity.ok("response"));
        when(ppGetOrderHelper.processGetOrderResponse(any())).thenReturn(getOrderResponse);
        when(ppCaptureOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(ppCaptureOrderHelper.processResponse(any())).thenReturn(captureResponse);

        paypalProviderHandler.reconTransaction(txn);

        assertEquals(TxnStatusEnum.SUCCESS.getName(), txn.getTxnStatus());
        assertEquals(1, txn.getRetryCount());
        verify(transactionDAO).updateTransactionForRecon(txn);
    }

    @Test
    void testApprovedStatusCaptureFailed() {
        PPOrder getOrderResponse = new PPOrder();
        getOrderResponse.setPaypalStatus(PaypalStatusEnum.APPROVED.getName());

        PPOrder captureResponse = new PPOrder();
        captureResponse.setPaypalStatus("FAILED");

        when(ppGetOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(httpServiceEngine.makeHttpCall(any())).thenReturn(ResponseEntity.ok("response"));
        when(ppGetOrderHelper.processGetOrderResponse(any())).thenReturn(getOrderResponse);
        when(ppCaptureOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(ppCaptureOrderHelper.processResponse(any())).thenReturn(captureResponse);

        paypalProviderHandler.reconTransaction(txn);

        // TxnStatus remains unchanged (PENDING)
        assertEquals(TxnStatusEnum.PENDING.getName(), txn.getTxnStatus());
        assertEquals(1, txn.getRetryCount());
        verify(transactionDAO).updateTransactionForRecon(txn);
    }

    @Test
    void testExceptionInGetOrder() {
        when(ppGetOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(httpServiceEngine.makeHttpCall(any())).thenThrow(new RuntimeException("Timeout"));

        paypalProviderHandler.reconTransaction(txn);

        // Status remains same
        assertEquals(TxnStatusEnum.PENDING.getName(), txn.getTxnStatus());
        assertEquals(1, txn.getRetryCount());
        verify(transactionDAO).updateTransactionForRecon(txn);
    }

    @Test
    void testRetryCountExceedsLimitWithoutException() {
        txn.setRetryCount(Constant.MAX_RETRY_ATTEMPT - 1); // Make it hit 3rd retry

        PPOrder successObj = new PPOrder();
        successObj.setPaypalStatus(PaypalStatusEnum.PAYER_ACTION_REQUIRED.getName());

        when(ppGetOrderHelper.prepareHttpRequest(any())).thenReturn(new HttpRequest());
        when(httpServiceEngine.makeHttpCall(any())).thenReturn(ResponseEntity.ok("response"));
        when(ppGetOrderHelper.processGetOrderResponse(any())).thenReturn(successObj);

        paypalProviderHandler.reconTransaction(txn);

        assertEquals(TxnStatusEnum.FAILED.getName(), txn.getTxnStatus());
        assertEquals(ErrorCodeEnum.RECON_PAYMENT_FAILED.getCode(), txn.getErrorCode());
        assertEquals(ErrorCodeEnum.RECON_PAYMENT_FAILED.getMessage(), txn.getErrorMessage());

        verify(transactionDAO).updateTransactionForRecon(txn);
    }
}
