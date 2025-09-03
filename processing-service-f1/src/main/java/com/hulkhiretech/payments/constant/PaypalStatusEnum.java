package com.hulkhiretech.payments.constant;

import lombok.Getter;

@Getter
public enum PaypalStatusEnum {

	PAYER_ACTION_REQUIRED("PAYER_ACTION_REQUIRED"),
	APPROVED("APPROVED"),
	COMPLETED("COMPLETED");

	private String name;

	private PaypalStatusEnum(String name) {
		this.name=name;
	}
	
	
	 public static PaypalStatusEnum fromString(String name) {
	        for (PaypalStatusEnum status : PaypalStatusEnum.values()) {
	            if (status.getName().equalsIgnoreCase(name)) {
	            	return status;
	            }
	        }
	        return null;
	    }

}
