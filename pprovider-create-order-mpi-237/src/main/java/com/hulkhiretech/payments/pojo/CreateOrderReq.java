package com.hulkhiretech.payments.pojo;

import lombok.Data;

@Data
public class CreateOrderReq {

	
	private String txnRef;
	private String amount;
	private String currency;
	private String returnUrl;
	private String cancelUrl;
}
