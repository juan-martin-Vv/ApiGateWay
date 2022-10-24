package com.jmvv.gateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
, properties = {"httpbin=http://localhost:${wiremock.server.port}"}
)
@AutoConfigureWireMock(port = 0)
class GatewayApplicationTests {

	@Autowired
	private WebTestClient webClient;

	private static WireMockServer wMockServer;
	@BeforeAll
	static void setup()
	{
		wMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
		wMockServer.start();
	}

	@AfterAll
	static void destruc(){
		wMockServer.stop();
	}

	@Test
	public void ServerTest(){
		System.out.println(wMockServer.baseUrl());
		assertTrue(wMockServer.isRunning());
	}

	
	@Test
	public void contextLoads() throws Exception {
		stubFor(get(urlEqualTo("/getloco"))
				.willReturn(aResponse()
						.withBody("{\"headers\":{\"loco\":\"pepe\"}}")
						.withHeader("Content-Type", "application/json")));
		stubFor(get(urlEqualTo("/delay/3"))
				.willReturn(aResponse()
						.withBody("no fallback")
						.withFixedDelay(3000)));

		System.out.println( webClient.get().uri("/get").exchange().expectBody().returnResult());
		webClient
				.get().uri("/get")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.headers.Hola").isEqualTo("mundo");

		webClient
				.get().uri("/delay/3")
				.header("Host", "www.circuitbreaker.com")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.consumeWith(
						response -> assertThat(response.getResponseBody()).isEqualTo("falla  en endpoint".getBytes()));
	}

}
