package com.hulkhiretech.payments.service.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.exception.ProcessingServiceException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypalprovider.PPErrorResponse;
import com.hulkhiretech.payments.paypalprovider.PPOrder;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PPGetOrderHelper {



	private final JsonUtil jsonUtils;
	
	@Value("${paypalprovider.getOrderUrl}")
	private String ppCaptureOrderUrl;
	
	public HttpRequest prepareHttpRequest(TransactionDTO txn) {
		HttpHeaders httpHeader=new HttpHeaders();
		
		httpHeader.setContentType(MediaType.APPLICATION_JSON);
		
		
		String url =ppCaptureOrderUrl;
		
		url=url.replace(Constant.ORDER_ID,txn.getProviderReference());
		
		HttpRequest httpRequest= new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.setHttpMethod(HttpMethod.GET);
		httpRequest.setHeaders(httpHeader);
		httpRequest.setRequestBody(Constant.EMPTY_STRING);
		httpRequest.setDestinationServiceName(Constant.PAYPAL_PROVIDER_SERVICE);
		return httpRequest;
	}
	
	public PPOrder processGetOrderResponse(ResponseEntity<String> getOrderResponse) {


  	String responseBody=getOrderResponse.getBody();

	log.info("responseBody:" + responseBody);


	if (getOrderResponse.getStatusCode()== HttpStatus.OK) {


		PPOrder resObj =jsonUtils.fromJson(responseBody, PPOrder.class);
		log.info("resObj:"+ resObj);


		if (resObj!=null && resObj.getOrderId()!=null 
				&& !resObj.getOrderId().isEmpty() 
				&& resObj.getPaypalStatus()!=null && !resObj.getPaypalStatus().isEmpty()) {

			//success scenario

			log.info("Success 200 with valid id sand status");
		

			log.info("resObj:{}", resObj);

			return resObj;

		}
		log.error("Success 200 but invalid id and status");


	}
	//failed response
	//if we get 4xx or 5xx from paypal, then we need to return the error respanse and send by paypal
	if  (getOrderResponse.getStatusCode().is4xxClientError()
			|| getOrderResponse.getStatusCode().is5xxServerError()) {
		
		log.error("paypal error response:{}",responseBody);
		
		PPErrorResponse errRes=jsonUtils.fromJson(responseBody, PPErrorResponse.class);
		
		log.info("errorRes:{}",errRes);
		
		
		throw new ProcessingServiceException(
				errRes.getErrorCode(),
				errRes.getErrorMessage(),
				HttpStatus.valueOf(getOrderResponse.getStatusCode().value()));
		
	}
	
	
	//anything other than 4xx or 5xx, generic exception handling
	log.error("Got unexpected error from Paypal processing."
	+"Returning GENERIC ERROR:{}",getOrderResponse);
	
	
	throw new ProcessingServiceException(
			ErrorCodeEnum.GENERIC_ERROR.getCode(),
			ErrorCodeEnum.GENERIC_ERROR.getMessage(),
			HttpStatus.INTERNAL_SERVER_ERROR);
	

}
}
