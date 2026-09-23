package com.dare.digitalbankingapi;

import org.springframework.boot.SpringApplication;

public class TestDigitalBankingApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(DigitalBankingApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
