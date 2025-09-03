package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PaypalProviderException extends RuntimeException {
    
	private static final long serialVersionUID = 6918567042499621898L;
	
	private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
    

    public PaypalProviderException(
    		String errorCode, String errorMessage,HttpStatus httpStatus) {
        super(errorMessage);  // set message in base RuntimeException
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus=httpStatus;
    }

}

