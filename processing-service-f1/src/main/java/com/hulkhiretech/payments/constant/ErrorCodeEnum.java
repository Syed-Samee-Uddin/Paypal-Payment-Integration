package com.hulkhiretech.payments.constant;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
    GENERIC_ERROR("30000", "Unable to process your request. Try again later"),
    UNABLE_TO_CONNECT_PAYPAL_PROVIDER("30001", 
    		"Unable to connect to PayPalProvider, please try again later"),
	
	RECON_PAYMENT_FAILED("30002","Recon Payment Failed. Transaction Failed after 3 attempts");


    private final String code;
    private final String message;

    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

   

//    public static ErrorCodeEnum fromCode(String code) {
//        for (ErrorCodeEnum error : values()) {
//            if (error.code.equals(code)) {
//                return error;
//            }
//        }
//        throw new IllegalArgumentException("Unknown error code: " + code);
//    }
}

