package br.com.navita.labs.mockconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do portal-demo (lab). Aplicação Spring Boot que no startup
 * busca configuração no Config Server (bootstrap) e expõe /portal com /ping para probes K8s.
 */
@SpringBootApplication
public class MockConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockConnectApplication.class, args);
    }
}
