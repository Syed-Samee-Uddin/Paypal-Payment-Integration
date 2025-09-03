package com.hulkhiretech.payments.constants;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
    GENERIC_ERROR("40000", "Unable to process your request. Try again later"),

	TEMP1_ERROR("40001", "TEMP1 error"),
    TEMP2_ERROR("40002", "TEMP2 error"),
    TEMP3_ERROR("40003", "TEMP3 error"),
    NAME_NULL("40004", "Name is Empty.Please check and try again"),
    UNABLE_TO_CONNECT_PAYPAL("40005", "Unable to connect to PayPal, please try again later"),
    PAYPAL_ERROR("40006","<PREPARE DYNAMIC MESSAGE from Paypal error response>");


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

