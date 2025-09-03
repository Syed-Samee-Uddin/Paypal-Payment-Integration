package com.hulkhiretech.payments.service.interfaces;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.Order;

public interface PaymentService {

	public Order createOrder(CreateOrderReq req);
	
	public Order getOrder(String orderId);
	public Order captureOrder(String orderId);
	
	
}
