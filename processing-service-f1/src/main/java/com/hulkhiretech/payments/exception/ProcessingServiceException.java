package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ProcessingServiceException extends RuntimeException {
	
    
	

	private static final long serialVersionUID = 7590743967742669105L;
	private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
    

    public ProcessingServiceException(
    		String errorCode, String errorMessage,HttpStatus httpStatus) {
        super(errorMessage);  // set message in base RuntimeException
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus=httpStatus;
    }

}

