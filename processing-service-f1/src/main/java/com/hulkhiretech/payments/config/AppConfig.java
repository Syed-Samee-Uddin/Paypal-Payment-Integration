package com.hulkhiretech.payments.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.entity.TransactionEntity;
import com.hulkhiretech.payments.util.converter.PaymentMethodEnumConverter;
import com.hulkhiretech.payments.util.converter.PaymentTypeEnumConverter;
import com.hulkhiretech.payments.util.converter.ProviderEnumConverter;
import com.hulkhiretech.payments.util.converter.TxnStatusEnumConverter;

@Configuration
public class AppConfig {

	@Bean
	ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("Async-Task-");
		executor.initialize();

		return executor;
	}
	
	@Bean
	ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(
				MatchingStrategies.STRICT);
		
		Converter<Integer, String> paymentMethodEnumConverter = new PaymentMethodEnumConverter();
        Converter<Integer, String> providerEnumConverter = new ProviderEnumConverter();
        // Define converters for TxnStatusEnum and PaymentTypeEnum if needed
        Converter<Integer, String> paymentTypeEnumConverter = new PaymentTypeEnumConverter();
        Converter<Integer, String> txnStatusEnumConverter = new TxnStatusEnumConverter();

        modelMapper.addMappings(new PropertyMap<TransactionEntity, TransactionDTO>() {
            @Override
            protected void configure() {
                using(paymentMethodEnumConverter).map(source.getPaymentMethodId(), destination.getPaymentMethod());
                using(providerEnumConverter).map(source.getProviderId(), destination.getProvider());
                // Add mappings for txnStatusId and paymentTypeId with their respective converters
                using(paymentTypeEnumConverter).map(source.getPaymentTypeId(), destination.getPaymentType());
                using(txnStatusEnumConverter).map(source.getTxnStatusId(), destination.getTxnStatus());
            }
        });
		
		return modelMapper;
	}
	
	 @Bean
	    RestClient restClient() {
	        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	        connectionManager.setMaxTotal(100);
	        connectionManager.setDefaultMaxPerRoute(20);

	        CloseableHttpClient httpClient = HttpClients.custom()
	            .setConnectionManager(connectionManager)
	            .evictIdleConnections(TimeValue.ofSeconds(30))
	            .build();

	        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
	        requestFactory.setConnectTimeout(10000);  // 10 seconds
	        requestFactory.setReadTimeout(15000);     // 15 seconds
	        requestFactory.setConnectionRequestTimeout(10000);

	        return RestClient.builder()
	            .requestFactory(requestFactory)
	            .build();
	    }
	
	
}
