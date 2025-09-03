package com.hulkhiretech.payments.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * uses jackson to convert java objects to json and vice versa
 * 
 */

@Component
@RequiredArgsConstructor
public class JsonUtil {
	
	private final ObjectMapper objectMapper;

	
	
	public  String toJson(Object obj) {
		
		String requestBodyAsJson=null;
		try {
			requestBodyAsJson = objectMapper.writerWithDefaultPrettyPrinter(
					).writeValueAsString(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return requestBodyAsJson;
	}
	
	
	
	public  <T> T fromJson(String json, Class<T> clazz) {
		T response=null; 
		
		try {
			 response = objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return response;
	}
	
	
		

}
