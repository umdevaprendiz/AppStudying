package com.example.AppStudying;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class AppStudyingApplication {

	public static void main(String[] args) {
		// Fixa o fuso do JVM em vez de depender do padrao do host: localmente
		// (Windows em America/Sao_Paulo) os horarios batiam, mas em producao
		// (container Linux, timezone padrao UTC) todo LocalDateTime.now() saia
		// 3h na frente do horario real de Brasilia.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		SpringApplication.run(AppStudyingApplication.class, args);
	}

}
