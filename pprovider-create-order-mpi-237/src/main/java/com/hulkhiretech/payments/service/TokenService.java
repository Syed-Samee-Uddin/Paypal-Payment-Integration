package com.hulkhiretech.payments.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.constants.Constants;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.OAuthToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

	private final HttpServiceEngine httpServiceEngine;

	@Value("${paypal.clientId}")
	private String clientId;

	@Value("${paypal.clientSecret}")
	private String clientSecret;

	@Value("${paypal.oAuthUrl}")
	private String oAuthUrl;
	
	private final ObjectMapper objectMapper;
	
	//TODO, this accessToken end-to-end lifecycle should be managed. Introduce Redis.
	private static String accessToken;
	
	/*
	 * Make call to redis for access token
	 * If not availabe,call paypal to generate new accessToken(Token is aftive for 8 hrs)
	 * save it to redis with expiry of 7 hrs(calculate from expires_in) 
	 * and return it.
	 * 
	 * when next call comes, then you again read from redis itself.
	 * the value will be valid, & you can directly return, without calling paypal. 
	 */

	public String getAccessToken() {
		log.info("getAccessToken called");
		
		if (accessToken != null) {
			log.info("getAccessToken returning|accessToken:" + accessToken);
			return accessToken;
		}

		log.info("Calling Paypal For Generating new AccessToken");
		HttpHeaders headerObj = new HttpHeaders();
		headerObj.setBasicAuth(clientId, clientSecret);
		headerObj.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
		formParams.add(Constants.GRANT_TYPE, Constants.CLIENT_CREDENTIALS);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setHttpMethod(HttpMethod.POST);
		httpRequest.setUrl(oAuthUrl);
		httpRequest.setHeaders(headerObj);
		httpRequest.setRequestBody(formParams);

		log.info("httpRequest:" + httpRequest);
		ResponseEntity<String> oAuthResponse = httpServiceEngine.makeHttpCall(httpRequest);
		
		String responseBody = oAuthResponse.getBody();
		log.info("responseBody:" + responseBody);
		
        try {
			OAuthToken oAuthObj = objectMapper.readValue(
					responseBody, OAuthToken.class);
			accessToken = oAuthObj.getAccessToken();
			log.info("accessToken:" + accessToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        log.info("getAccessToken returning|accessToken:" + accessToken);
		return accessToken;
	}

}
