package com.hulkhiretech.payments.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class TransactionDTO {
    private Integer id;
    private Integer userId;

    private String paymentMethod;
    private String provider;
    private String paymentType;
    private String txnStatus;

    private BigDecimal amount;
    private String currency;

    private String merchantTransactionReference;
    private String txnReference;
    private String providerReference;

    private String errorCode;
    private String errorMessage;

    private Timestamp creationDate;
    private Integer retryCount;
}
