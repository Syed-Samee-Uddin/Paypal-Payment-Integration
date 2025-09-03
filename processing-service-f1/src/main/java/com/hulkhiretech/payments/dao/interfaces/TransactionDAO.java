package com.hulkhiretech.payments.dao.interfaces;

import java.util.List;

import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.entity.TransactionEntity;

public interface TransactionDAO {

	
	public List<TransactionEntity>  loadTransactionsForRecon();

	public void updateTransactionForRecon(TransactionDTO txn);
	
}
