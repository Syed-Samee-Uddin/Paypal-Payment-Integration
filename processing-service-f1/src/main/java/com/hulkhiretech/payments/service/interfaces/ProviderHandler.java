package com.hulkhiretech.payments.service.interfaces;

import com.hulkhiretech.payments.dto.TransactionDTO;

public interface ProviderHandler {
	
	void reconTransaction(TransactionDTO txn);

}
			