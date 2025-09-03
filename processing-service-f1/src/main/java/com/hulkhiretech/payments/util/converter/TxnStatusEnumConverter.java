package com.hulkhiretech.payments.util.converter;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payments.constant.TxnStatusEnum;

public class TxnStatusEnumConverter extends AbstractConverter<Integer,String>{
	
	@Override
	protected String convert(Integer source) {
		return TxnStatusEnum.fromId(source).getName();
	}

}
