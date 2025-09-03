package com.hulkhiretech.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hulkhiretech.payments.constant.PaypalStatusEnum;
import com.hulkhiretech.payments.constant.TxnStatusEnum;
import com.hulkhiretech.payments.dao.interfaces.TransactionDAO;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypalprovider.PPOrder;
import com.hulkhiretech.payments.service.helper.PPGetOrderHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PaypalProviderHandlerTest {
	
	@Mock
	private  HttpServiceEngine httpServiceEngine;
	@Mock
	private  PPGetOrderHelper ppGetOrderHelper;
	@Mock
	private  TransactionDAO transactionDAO;


	
	@InjectMocks
	private PaypalProviderHandler PaypalProviderHandler;

	@Test
	public void testMethodCompletedPaypalProviderCase() {
	
		log.info("test method executed");
		
		//Arrange
		TransactionDTO txn=new TransactionDTO();
		txn.setRetryCount(0);
		txn.setTxnStatus(TxnStatusEnum.PENDING.name());
		
		//PPOrder successObj=ppGetOrderHelper.processGetOrderResponse(response);
		
		PPOrder successObj=new PPOrder();
		successObj.setPaypalStatus("COMPLETED");
		successObj.setOrderId("12345");
		
		//Mocking the behaviour of ppGetOrderHelper
		
		when(ppGetOrderHelper.processGetOrderResponse(any())
				).thenReturn(successObj);
				

		
		//Act
		PaypalProviderHandler.reconTransaction(txn);
		
		//Verify
		assertEquals(TxnStatusEnum.SUCCESS.getName(), txn.getTxnStatus());
		assertEquals(1,txn.getRetryCount());
		
		//transactionDAO.updateTransactionForRecon(txn); ensure this method is called one time
		
		verify(transactionDAO,times(1)).updateTransactionForRecon(txn);

	}
	
	
	@Test
	public void testMethodPayerActionRequiredPaypalProviderCase() {
	
		log.info("test method executed");
		
		//Arrange
		TransactionDTO txn=new TransactionDTO();
		txn.setRetryCount(0);
		txn.setTxnStatus(TxnStatusEnum.PENDING.name());
		
		//PPOrder successObj=ppGetOrderHelper.processGetOrderResponse(response);
		
		PPOrder successObj=new PPOrder();
		successObj.setPaypalStatus(PaypalStatusEnum.PAYER_ACTION_REQUIRED.getName());
		successObj.setOrderId("12345");
		
		//Mocking the behaviour of ppGetOrderHelper
		
		when(ppGetOrderHelper.processGetOrderResponse(any())
				).thenReturn(successObj);
				

		
		//Act
		PaypalProviderHandler.reconTransaction(txn);
		
		//Verify
		assertEquals(TxnStatusEnum.PENDING.name(), txn.getTxnStatus());
		assertEquals(1,txn.getRetryCount());
		
		//transactionDAO.updateTransactionForRecon(txn); ensure this method is called one time
		
		verify(transactionDAO,times(1)).updateTransactionForRecon(txn);

	}
}
