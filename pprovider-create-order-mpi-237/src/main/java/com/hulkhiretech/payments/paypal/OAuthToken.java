package com.hulkhiretech.payments.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OAuthToken {
	
	 private String scope;

	    @JsonProperty("access_token")
	    private String accessToken;

	    @JsonProperty("token_type")
	    private String tokenType;

	    @JsonProperty("app_id")
	    private String appId;

	    @JsonProperty("expires_in")
	    private int expiresIn;

	    private String nonce;

}
