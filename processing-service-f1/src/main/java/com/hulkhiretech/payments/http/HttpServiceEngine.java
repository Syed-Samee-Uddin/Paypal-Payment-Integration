package com.hulkhiretech.payments.http;

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
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpServiceEngine {

	private RestClient restClient;

	public HttpServiceEngine(RestClient restClient) {
		this.restClient=restClient;

		log.info("restClient created|restClient:" + restClient);
	}
	@CircuitBreaker(name = "payment-processing-service", fallbackMethod = "fallbackProcessPayment")
	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {
		log.info("makeHttpCall called|httpRequest:" + httpRequest);

		try {
		
			ResponseEntity<String> responseEntity = restClient.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(header -> header.addAll(httpRequest.getHeaders()))
					.body(httpRequest.getRequestBody())
					.retrieve()
					.toEntity(String.class);
			log.info("responseEntity:" + responseEntity);
			return responseEntity;

		} catch(HttpClientErrorException | HttpServerErrorException e) {
			log.error("HTTP error occurred: {}", e.getStatusCode(), e);

		    HttpStatusCode status = e.getStatusCode();

		    // Check for Service Unavailable (503) or Gateway Timeout (504)
		    if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {

		    	throw new ProcessingServiceException(
						ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getCode(),
						ErrorCodeEnum.UNABLE_TO_CONNECT_PAYPAL_PROVIDER.getMessage(), 
						HttpStatus.valueOf(e.getStatusCode().value()));
		    }

		    // For other client/server errors, return the error response body as-is
		    String errorJson = e.getResponseBodyAsString(); // PayPal may send structured error JSON
		    return ResponseEntity.status(status).body(errorJson);
			
		} catch (Exception e) {// timeout case - No response from paylal
			log.error("Exception occurred while making HTTP call: {}", e.getMessage(), e);
			
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
