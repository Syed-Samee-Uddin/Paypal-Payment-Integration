package com.hulkhiretech.payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.hulkhiretech.payments.constants.ErrorCodeEnum;
import com.hulkhiretech.payments.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaypalProviderException.class)
    public ResponseEntity<ErrorResponse> handlePaypalException(
    		PaypalProviderException ex) {
    	
        log.error("PaypalProviderException: {}", ex.getMessage(), ex);
        
    	ErrorResponse errorResponse = new ErrorResponse(
    			ex.getErrorCode(), ex.getMessage());
        
		log.error("ErrorResponse: {}", errorResponse);
		return new ResponseEntity<>(errorResponse,
				ex.getHttpStatus()); // or any relevant status
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        
    	ErrorResponse errorResponse = new ErrorResponse(
        		ErrorCodeEnum.GENERIC_ERROR.getCode(), ErrorCodeEnum.GENERIC_ERROR.getMessage()
        		);
        
    	log.info("Returning GenericException:{}",ex.getMessage(),ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
