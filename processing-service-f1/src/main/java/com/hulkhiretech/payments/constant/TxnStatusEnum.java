package com.hulkhiretech.payments.constant;

import lombok.Getter;

@Getter
public enum TxnStatusEnum {
    CREATED(1, "CREATED"),
    INITIATED(2, "INITIATED"),
    PENDING(3, "PENDING"),
    APPROVED(4, "APPROVED"),
    SUCCESS(5, "SUCCESS"),
    FAILED(6, "FAILED");

    private final int id;
    private final String name;

    TxnStatusEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static TxnStatusEnum fromId(int id) {
        for (TxnStatusEnum status : values()) {
            if (status.id == id) return status;
        }
        return null;
    }

    public static TxnStatusEnum fromName(String name) {
        for (TxnStatusEnum status : values()) {
            if (status.name.equalsIgnoreCase(name)) return status;
        }
        return null;
    }
}
