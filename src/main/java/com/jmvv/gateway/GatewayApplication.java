package com.jmvv.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@SpringBootApplication
@EnableConfigurationProperties(configUri.class)
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RouteLocator misRutas(RouteLocatorBuilder builder, configUri uri) {
		String uril = uri.uril();
		return builder.routes()
				.route(r -> r
						.path("/get")
						.filters(f -> f.addRequestHeader("hola", "mundo"))
						.uri(uril))
		// 
				.route(p -> p
						.host("*.circuitbreaker.com")
						.filters(f -> f.circuitBreaker(config -> config
								.setName("mycmd")
								.setFallbackUri("forware:/fallback")))
						.uri(uril))
				.build();
	}

	@RequestMapping("/fallback")
	public Mono<String> falla() {
		return Mono.just("falla  en endpoint");
	}
}

@ConfigurationProperties
record configUri(String uril) {
	@ConstructorBinding
	public configUri(String uril) {
		this.uril = "http://httpbin.org:80";
	}
}