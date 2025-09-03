package com.hulkhiretech.payments.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.dao.interfaces.TransactionDAO;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.entity.TransactionEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReconService {
	
	private final TransactionDAO transactionDAO;
	
	private final ReconAsync reconAsync;
	
	private final ModelMapper modelMapper;
	
	//@Scheduled(cron = "0 0/10 * * * ? ")
	public void reconTransactions() {
		log.info("ReconService.recon() called");
		
		
		
		// Add your reconciliation logic here
		
		/*
		 * Get List<TransactionEntity> from DB, for Recon.
		 */
		List<TransactionEntity> txnForRecon = transactionDAO.loadTransactionsForRecon();
		log.info("ReconService.recon() - txnForRecon.size(): {}", txnForRecon.size());
		
		List<TransactionDTO> txnDTOList = convertToDtoList(txnForRecon);
		log.info("ReconService.recon() - txnDTOList.size(): {}", txnDTOList.size());
		
		
		// iterate through each transaction, & perform reconciliation
		txnDTOList.forEach(txn -> {
			// Perform reconciliation logic here
			log.info("ReconService.recon() - txn: {}", txn);
			reconAsync.reconTransactionAsyc(txn);
		});
		
	}
	
	private List<TransactionDTO> convertToDtoList(List<TransactionEntity> entityList) {
		return entityList.stream()
                .map(entity -> modelMapper.map(entity, TransactionDTO.class))
                .toList();
    }
	
	
	
	

}
