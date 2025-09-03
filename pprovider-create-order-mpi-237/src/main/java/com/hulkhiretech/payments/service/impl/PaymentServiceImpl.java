package com.hulkhiretech.payments.service.impl;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constants.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.Link;
import com.hulkhiretech.payments.paypal.PayPalOrder;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.Order;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.helper.CaptureOrderHelper;
import com.hulkhiretech.payments.service.helper.CreateOrderHelper;
import com.hulkhiretech.payments.service.helper.GetOrderHelper;
import com.hulkhiretech.payments.service.interfaces.PaymentService;
import com.hulkhiretech.payments.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final TokenService tokenService;

	private final HttpServiceEngine httpServiceEngine;

	private final CreateOrderHelper createOrderHelper;

	private final GetOrderHelper getOrderHelper;

	private final CaptureOrderHelper captureOrderHelper;

	private final JsonUtils jsonUtils;

	@Override
	public Order createOrder(CreateOrderReq req) {
		log.info("createOrder called|req:{}", req);

		/*
		 * 1. Call OAuth API of paypal to get access token
		 * 2. Call create order API of paypal with access token
		 * 3. Handle response as success or failure
		 * 4. Return response to controller
		 */

		String accessToken = tokenService.getAccessToken();

		HttpRequest httpRequest = createOrderHelper.prepareHttpRequestForCreateOrder(req, accessToken);

		ResponseEntity<String> createOrderResponse = httpServiceEngine.makeHttpCall(httpRequest);

		String responseBody = createOrderResponse.getBody();
		log.info("responseBody:" + responseBody);


		PayPalOrder resObj = jsonUtils.fromJson(responseBody, PayPalOrder.class);
		log.info("resObj:" + resObj);

		Order orderRes = new Order();
		orderRes.setOrderId(resObj.getId());
		orderRes.setPaypalStatus(resObj.getStatus());

		Optional<String> opRedirectUrl = resObj.getLinks().stream()
				.filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
				.map(Link::getHref)
				.findFirst();

		orderRes.setRedirectUrl(opRedirectUrl.orElse(null));

		log.info("orderRes:{}", orderRes);

		return orderRes;
	}

	@Override
	public Order getOrder(String orderId) {

		if(orderId.contains("TEMP1")) {
			throw new PaypalProviderException(
					ErrorCodeEnum.TEMP1_ERROR.getCode(), 
					ErrorCodeEnum.TEMP1_ERROR.getMessage(),
					HttpStatus.BAD_REQUEST);
			// 400 BAD REQUEST
		}

		if(orderId.contains("TEMP3")) {
			throw new PaypalProviderException(
					ErrorCodeEnum.TEMP3_ERROR.getCode(), 
					ErrorCodeEnum.TEMP3_ERROR.getMessage(),
					HttpStatus.BAD_REQUEST);
			// 400 BAD REQUEST
		}

		/*
		String name = null;
		if(name == null) {
			throw new PaypalProviderException(
					ErrorCodeEnum.NAME_NULL.getCode(), 
					ErrorCodeEnum.NAME_NULL.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
			// 500 INTERNAL SERVER ERROR
		}
		name.length();
		 */

		String accessToken = tokenService.getAccessToken();

		HttpRequest httpRequest = getOrderHelper.prepareHttpRequest(orderId, accessToken);

		ResponseEntity<String> getOrderResponse = httpServiceEngine.makeHttpCall(httpRequest);

		Order response = getOrderHelper.processGetOrderResponse(getOrderResponse);
		log.info("response:" + response);
		return response;
	}

	@Override
	public Order captureOrder(String orderId) {

		String accessToken = tokenService.getAccessToken();

		HttpRequest httpRequest = captureOrderHelper.prepareHttpRequest(orderId, accessToken);

		ResponseEntity<String> captureOrderResponse = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("captureOrderResponse:" + captureOrderResponse);
		
		Order orderRes = captureOrderHelper.processResponse(captureOrderResponse);
		log.info("response:" + orderRes);

		return orderRes;
	}

}
