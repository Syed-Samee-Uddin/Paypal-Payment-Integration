package com.hulkhiretech.payments.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
	
	
	private String orderId;
	private String paypalStatus;
	private String redirectUrl;
	

}
