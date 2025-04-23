package br.com.vnrg.mtls.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


@ComponentScan("br.com.vnrg")
@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class SpringBootMtlsTestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMtlsTestApplication.class, args);
    }


    private final RestTemplate restTemplate;


    @Override
    public void run(String... args) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = this.restTemplate.exchange(
                    "https://localhost:8443/todo",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("Response: {}", response);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            System.exit(0);
        }

    }
}
