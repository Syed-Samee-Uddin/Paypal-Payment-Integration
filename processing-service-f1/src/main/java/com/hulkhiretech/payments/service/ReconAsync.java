package com.hulkhiretech.payments.service;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.ProviderEnum;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.service.impl.PaypalProviderHandler;
import com.hulkhiretech.payments.service.interfaces.ProviderHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReconAsync {
	
	private final ApplicationContext applicationContext;
	
	@Async
	public void reconTransactionAsyc(TransactionDTO txn) {
		log.info("ReconAsync.reconTransactionAsyc() called txn: {}", txn);
		// Business logic for Recon.
		
		ProviderHandler providerHandler = null;
		if(txn.getProvider().equals(ProviderEnum.PAYPAL.getName())) {
			providerHandler = applicationContext.getBean(PaypalProviderHandler.class);
		}
		
		if(providerHandler == null) {
			log.error("ReconAsync.reconTransactionAsyc() - "
                    + "providerHandler is null for txn: {}", txn);
            return;
		}
		
		providerHandler.reconTransaction(txn);
		
	}

}
