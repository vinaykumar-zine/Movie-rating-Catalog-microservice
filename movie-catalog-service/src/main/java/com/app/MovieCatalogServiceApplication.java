package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class MovieCatalogServiceApplication {
	
	@Bean
	@LoadBalanced //this annotation means telling the Eureka that the url I'm sending you is not an actual url 
	public RestClient getRestClient() {
		return RestClient.create();
	}

	public static void main(String[] args) {
		SpringApplication.run(MovieCatalogServiceApplication.class, args);
	}

}
