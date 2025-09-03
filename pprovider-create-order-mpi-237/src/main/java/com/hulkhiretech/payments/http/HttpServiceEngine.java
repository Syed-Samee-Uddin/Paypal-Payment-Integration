 	package com.hulkhiretech.payments.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payments.constants.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpServiceEngine {
	
	private RestClient restClientInstance;
	public HttpServiceEngine(RestClient restClient) {
		this.restClientInstance = restClient;
		log.info("restClient created| restClinet: "+restClientInstance);
	}
	
	public ResponseEntity<String> 	makeHttpCall(HttpRequest httpRequest) {
				
		log.info("makehttpcall called|httpRequest:"+httpRequest);
		
		try {
			ResponseEntity<String> responseEntity=restClientInstance.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(header ->header.addAll(httpRequest.getHeaders()))
					.body(httpRequest.getRequestBody())
					.retrieve()
					.toEntity(String.class);
			
			log.info("responseEntity:"+ responseEntity);
			
			return responseEntity;
			
		}
		
		catch(HttpClientErrorException | HttpServerErrorException  e) {
 			log.error("Http error occurred: {}", e.getStatusCode(), e);

		    HttpStatusCode status = e.getStatusCode();
		    if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
		        throw new PaypalProviderException(ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL.getCode(),
		        		ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL.getMessage(),
		        		HttpStatus.valueOf(e.getStatusCode().value()));
		    }
		        // Return error response as JSON string
		        String errorJson = e.getResponseBodyAsString();
		        return ResponseEntity.status(status).body(errorJson);
			
		}
		
		catch(Exception e) {// this is a time out
			log.error("Exception occured while making a HTTP call:{}",e.getMessage(),e);
			  throw new PaypalProviderException(ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL.getCode(),
		        		ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL.getMessage(),
		        		HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	

}
