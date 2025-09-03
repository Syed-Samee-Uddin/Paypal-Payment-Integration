package com.hulkhiretech.payments.constant;

import lombok.Getter;

@Getter
public enum PaymentTypeEnum {
    SALE(1, "SALE");

    private final int id;
    private final String name;

    PaymentTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static PaymentTypeEnum fromId(int id) {
        for (PaymentTypeEnum type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }

    public static PaymentTypeEnum fromName(String name) {
        for (PaymentTypeEnum type : values()) {
            if (type.name.equalsIgnoreCase(name)) return type;
        }
        return null;
    }
}

