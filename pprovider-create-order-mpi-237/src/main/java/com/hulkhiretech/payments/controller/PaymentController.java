package com.hulkhiretech.payments.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.constants.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.Order;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/paypal/order")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;
	




	//	private PaymentService paymentService;
	//	public PaymentController(PaymentService paymentService) {
	//		this.paymentService=paymentService;
	//		log.info("paymentController constructor called|paymentService:{}",
	//				  paymentService);
	//		
	//	}




	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Order createOrder(@RequestBody CreateOrderReq createOrderReq) {

		log.info("createorderrequest:{}", createOrderReq);

		Order response=paymentService.createOrder(createOrderReq);

		log.info("response:{}",response);

		return response;
	}


	@GetMapping("/{orderId}")
	public Order getOrder(
			@PathVariable String orderId) {

		log.info("getOrder orderId:{}", orderId);
		
		if (orderId.contains("TEMP2")) {
			throw new PaypalProviderException(
					ErrorCodeEnum.TEMP2_ERROR.getCode(),
					ErrorCodeEnum.TEMP2_ERROR.getMessage(),
					HttpStatus.BAD_REQUEST);
			}
		
		Order response=null;
		response = paymentService.getOrder(orderId);



		log.info(orderId);



		log.info("response:{}", response);

		return response;
	}

	@PostMapping("/{orderId}/capture")
	public Order captureOrder(
			@PathVariable String orderId) {
		log.info("captureOrder orderID:{}",orderId);

		Order response =paymentService.captureOrder(orderId);

		log.info("response:{}",response);

		return response;
	}

}
