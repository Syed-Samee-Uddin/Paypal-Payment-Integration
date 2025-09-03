package com.hulkhiretech.payments.paypalprovider;

import lombok.Data;

@Data
public class PPErrorResponse {

	private String errorCode;
    private String errorMessage;

    public PPErrorResponse(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
