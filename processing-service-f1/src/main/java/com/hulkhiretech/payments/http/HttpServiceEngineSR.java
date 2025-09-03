 	package com.hulkhiretech.payments.http;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.ProcessingServiceException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

//import com.hulkhiretech.payments.constants.ErrorCodeEnum;
//import com.hulkhiretech.payments.exception.PaypalProviderException;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class HttpServiceEngineSR {
	
	private RestClient restClient;
	private LoadBalancerClient loadBalancerClient;
	
	public HttpServiceEngineSR(RestClient restClient, LoadBalancerClient loadBalancerClient) {
		this.restClient=restClient;
		this.loadBalancerClient=loadBalancerClient;
	}
	
	
	@CircuitBreaker(name = "payment-processing-service", fallbackMethod = "fallbackProcessPayment")
	public ResponseEntity<String> 	makeHttpCall(HttpRequest httpRequest) {
				
		log.info("makehttpcall called|httpRequest:"+httpRequest);
		
		try {
			// if destinationServiceName is also null,then throw exception
			
			if (httpRequest.getDestinationServiceName()==null) {
				log.error("No destination service anme provided");
				throw new ProcessingServiceException(ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getCode(),
						ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			ServiceInstance destinationService=loadBalancerClient.choose(
					httpRequest.getDestinationServiceName());
			
			if (destinationService==null) {
				log.error("No avaliable service instance found for paypal-provider-service ");
				throw new ProcessingServiceException(ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getCode(),
						ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			//Cconstruct the url using the service instance's URI
			
			String url=destinationService.getUri()+httpRequest.getUrl();
			log.info("url:" + url);
			
			ResponseEntity<String> responseEntity=restClient.method(httpRequest.getHttpMethod())
					.uri(url)
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
		    	throw new ProcessingServiceException(
						ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getCode(),
						ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getMessage(),
						HttpStatus.valueOf(e.getStatusCode().value()));
		    	
		        		
		    }
		    
		    
		        // Return error response as JSON string
		        String errorJson = e.getResponseBodyAsString();
		        return ResponseEntity.status(status).body(errorJson);
			
		}
		
		catch(Exception e) {// this is a time out
			log.error("Exception occured while making a HTTP call:{}",e.getMessage(),e);
			
			
			
			throw new ProcessingServiceException(
					ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getCode(),
					ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
			
			
			
		}
	}
	
	public ResponseEntity<String> fallbackProcessPayment(HttpRequest httpRequest, Throwable t) {
		// Handle fallback logic here
		log.error("Fallback method called due to: {}", t.getMessage(), t);
		throw new ProcessingServiceException(
				ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getCode(),
				ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	

}
