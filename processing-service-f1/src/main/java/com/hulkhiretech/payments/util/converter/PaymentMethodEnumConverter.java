package com.hulkhiretech.payments.util.converter;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payments.constant.PaymentMethodEnum;

public class PaymentMethodEnumConverter extends AbstractConverter<Integer,String>{
	
	@Override
	protected String convert(Integer source) {
		return PaymentMethodEnum.fromId(source).getName();
	}

}
