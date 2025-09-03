package com.hulkhiretech.payments.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
import com.hulkhiretech.payments.service.interfaces.ProviderHandler;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaypalProviderHandler implements ProviderHandler {


	private final HttpServiceEngine httpServiceEngine;
	private final PPGetOrderHelper ppGetOrderHelper;

	private final JsonUtil jsonUtils;




	private final PPCaptureOrderHelper ppCaptureOrderHelper;


	private final TransactionDAO transactionDAO;
	
	/**
	 * 1.For each of the payment, we need to call PayPal GetOrder API
	   		PaypalStatus:PAYER_ACTION_REQUIRED
	   		PaypalStatus:APPROVED
	   		PaypalStatus:COMPLETED
	   	2.PaypalStatus:PAYER_ACTION_REQUIRED
	   		then no change at our end.Just Increment retry attempt(1)
	   	3.PaypalStatus:APPROVED
	   		call CaptureAPI
	   			SUCCESS
	   	4.PaypalStatus:COMPLETED
	   		update our txn as SUCCESS


	 */

	@Override
	public void reconTransaction(TransactionDTO txn) {

		log.info("PaypalProviderHandler.reconTransaction() called txn:{}",txn);

		txn.setRetryCount(txn.getRetryCount()+1);

		String initialTxnStatus = txn.getTxnStatus();

		boolean isExceptionWhileProcessing = false; 

		try {

			PPOrder successObj = getOrderFromPP(txn);

			log.info("PaypalProviderHandler.reconTransaction() - " + "successObj: {}",successObj);

			PaypalStatusEnum statusEnum=PaypalStatusEnum.fromString(
					successObj.getPaypalStatus());

			switch(statusEnum) {

			case PAYER_ACTION_REQUIRED:
				log.info("PaypalProviderHandler.reconTransaction() - " + "PaypalStatus:PAYER_ACTION_REQUIRED");
				//NO ACTION
				break;
			case APPROVED:
				log.info("PaypalProviderHandler.reconTransaction() - " + "PaypalStatus:APPROVED");


				//call CaptureAPI
				PPOrder captureRes=ppCaptureOrder(txn);  //TEMP
				if (captureRes.getPaypalStatus().equals(
						PaypalStatusEnum.COMPLETED.getName())) {

					// if capture is success then update our txn as SUCCESS
					txn.setTxnStatus(TxnStatusEnum.SUCCESS.getName());

				}else {
					log.error("PaypalProviderHandler.reconTransaction() - "+ "CaptureAPI failed, paypal status NOT COMPLETED");
				}
				//if failed then wait for next cycle
				break;
			case COMPLETED:
				log.info("PaypalProviderHandler.reconTransaction() - " + "PaypalStatus:COMPLETED");
				//update our txn as success
				txn.setTxnStatus(TxnStatusEnum.SUCCESS.getName());
				break;
			default:
				log.error("PaypalProviderHandler.reconTransaction() - " + "Unknown PaypalStatus");

			}


		}catch(Exception e) {
			log.error("PaypalProviderHandler.reconTransaction() - "+ "Exception:{}",e);
			isExceptionWhileProcessing= true; 

		}

		// if initialTxnStatus is not equal to txn.getTxnStatus(), then call transactionDAO.updateTransactionForRecon()
		if (!initialTxnStatus.equals(txn.getTxnStatus())) {
			log.info("PaypalProviderHandler.reconTransaction() - " + "initialTxnStatus: {}, txn.getTxnStatus(): {}",
					initialTxnStatus, txn.getTxnStatus());
			transactionDAO.updateTransactionForRecon(txn);
			return;
		}
		// if txn.getRetryCount() >= 3, then update txn as FAILED
		if (txn.getRetryCount() >= Constant.MAX_RETRY_ATTEMPT && !isExceptionWhileProcessing) {
			log.info("PaypalProviderHandler.reconTransaction() - " + "txn.getRetryCount(): {}", txn.getRetryCount());
			txn.setTxnStatus(TxnStatusEnum.FAILED.getName());
			txn.setErrorCode(ErrorCodeEnum.RECON_PAYMENT_FAILED.getCode());
			txn.setErrorMessage(ErrorCodeEnum.RECON_PAYMENT_FAILED.getMessage());
		}

		transactionDAO.updateTransactionForRecon(txn); // Update retry Count in DB.
		log.info("Updated Txn in DB txn: {}", txn);



	}


	private PPOrder getOrderFromPP(TransactionDTO txn) {
		HttpRequest httpRequest = ppGetOrderHelper.prepareHttpRequest(txn);

		log.info("PaypalProviderHandler.reconTransaction() - " + "httpRequest: {}", httpRequest);

		ResponseEntity<String> response=httpServiceEngine.makeHttpCall(httpRequest);

		log.info("PaypalProviderHandler.reconTransaction() - " + "response: {}",response);

		PPOrder successObj=ppGetOrderHelper.processGetOrderResponse(response);
		
		return successObj;
	}


	private PPOrder ppCaptureOrder(TransactionDTO txn) {
		HttpRequest httpRequest = ppCaptureOrderHelper.prepareHttpRequest(txn);

		log.info("PaypalProviderHandler.reconTransaction() - " + "httpRequest: {}", httpRequest);

		ResponseEntity<String> response=httpServiceEngine.makeHttpCall(httpRequest);

		log.info("PaypalProviderHandler.reconTransaction() - " + "response: {}",response);

		PPOrder successObj=ppCaptureOrderHelper.processResponse(response);
		return successObj;
	}

}
