package com.hulkhiretech.payments.service.helper;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.hulkhiretech.payments.constants.Constants;
import com.hulkhiretech.payments.constants.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.Link;
import com.hulkhiretech.payments.paypal.PayPalOrder;
import com.hulkhiretech.payments.pojo.Order;
import com.hulkhiretech.payments.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaptureOrderHelper {

	@Value("${paypal.captureOrderUrl}")
	private String captureOrderUrl;
	
	private final JsonUtils jsonUtils;
	

	public HttpRequest prepareHttpRequest(String orderId, String accessToken) {
		HttpHeaders headerObj = new HttpHeaders();
		headerObj.setBearerAuth(accessToken);
		headerObj.setContentType(MediaType.APPLICATION_JSON);

		String url = captureOrderUrl;
		url = url.replace("{orderId}", orderId);
		
		// Create HttpRequest object		
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);

		httpRequest.setUrl(url);
		httpRequest.setHeaders(headerObj);
		httpRequest.setRequestBody(Constants.EMPTY_STRING);
		log.info("httpRequest:" + httpRequest);
		return httpRequest;
	}
	
	public Order processResponse(ResponseEntity<String> captureOrderResponse) {
		String responseBody = captureOrderResponse.getBody();
		log.info("responseBody:" + responseBody);

		if (captureOrderResponse.getStatusCode().is2xxSuccessful()) {
			PayPalOrder resObj = jsonUtils.fromJson(responseBody, PayPalOrder.class);
			log.info("resObj:" + resObj);

			if (resObj != null 
					&& resObj.getId() != null && !resObj.getId().isEmpty() 
					&& resObj.getStatus() != null && !resObj.getStatus().isEmpty()) {
				// SUCCESS scenario

				log.info("SUCCESS with valid id and status");

				Order orderRes = new Order();
				orderRes.setOrderId(resObj.getId());
				orderRes.setPaypalStatus(resObj.getStatus());

				Optional<String> opRedirectUrl = resObj.getLinks().stream()
						.filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
						.map(Link::getHref)
						.findFirst();

				orderRes.setRedirectUrl(opRedirectUrl.orElse(null));

				log.info("orderRes:{}", orderRes);

				return orderRes;

			} 
			log.error("SUCCESS 200 but invalid id or status");
		}

		// Failed response
		//if we get 4xx or 5xx from paypal, then we need to return the error response as it is
		//if we get 4xx or 5xx from paypal, then we need to return the error respanse and send by paypal
		if  (captureOrderResponse.getStatusCode().is4xxClientError()
				|| captureOrderResponse.getStatusCode().is5xxServerError()) {
			
			log.error("paypal error response:{}",responseBody);
			throw new PaypalProviderException(
					ErrorCodeEnum.PAYPAL_ERROR.getCode(),
					"this was done using open ai",//TODO
					HttpStatus.valueOf(captureOrderResponse.getStatusCode().value()));
		}


		// Anything other than 4xx or 5xx, generic exception handling.
		log.error("Got unexpected response from Paypal processing. "
				+ "Returnign GENERIC ERROR: {}", captureOrderResponse);

		throw new PaypalProviderException( 
				ErrorCodeEnum.GENERIC_ERROR.getCode(),
				ErrorCodeEnum.GENERIC_ERROR.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
