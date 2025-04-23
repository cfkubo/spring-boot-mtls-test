package br.com.vnrg.mtls.test;


import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


@Slf4j
@Configuration
public class RestClientConfig {

    // Load keystore and truststore locations and passwords
    @Value("${client.ssl.trust-store}")
    private Resource trustStore;
    @Value("${client.ssl.key-store}")
    private Resource keyStore;
    @Value("${client.ssl.trust-store-password}")
    private String trustStorePassword;
    @Value("${client.ssl.key-store-password}")
    private String keyStorePassword;


    @Bean
    public RestTemplate restTemplate() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(keyStore.getInputStream(), keyStorePassword.toCharArray());

            KeyStore ts = KeyStore.getInstance("PKCS12");
            ts.load(trustStore.getInputStream(), trustStorePassword.toCharArray());


            // Set up SSL context with truststore and keystore
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(ks, keyStorePassword.toCharArray())
                    .loadTrustMaterial(ts, null)
                    .build();

            // Configure the SSLConnectionSocketFactory to use NoopHostnameVerifier
            SSLConnectionSocketFactory sslConFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

            // Use a connection manager with the SSL socket factory
            HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslConFactory)
                    .build();

            // Build the CloseableHttpClient and set the connection manager
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .build();

            // Set the HttpClient as the request factory for the RestTemplate
             ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

            return new RestTemplate(requestFactory);




            // Build TLS strategy
//            var tlsStrategy = ClientTlsStrategyBuilder.create()
//                    .setSslContext(sslContext)
//                    .build();
//
//            // Build connection manager with the TLS strategy
//            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
//                    .setTlsStrategy(tlsStrategy)
//                    .build();
//
//            // Build the HttpClient
//            return HttpClients.custom()
//                    .setConnectionManager(connectionManager)
//                    .setDefaultSocketConfig(SocketConfig.custom()
//                            .setSoTimeout(30_000, java.util.concurrent.TimeUnit.MILLISECONDS)
//                            .build())
//                    .evictIdleConnections(Duration.ofSeconds(30))
//                    .build();



//
//            // Usa DefaultClientTlsStrategy (recomendado pelo javadoc do HttpClient 5)
//            DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);
//
//
//            PoolingAsyncClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
//                    .setTlsStrategy(tlsStrategy)
//                    .build();
//
//            CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
//                    .setConnectionManager(connManager)
//                    .build();



//            TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
//                    .setSslContext(sslContext)
//                    .build();
//
//            PoolingHttpClientConnectionManager cm =
//                    PoolingHttpClientConnectionManagerBuilder.create()
//                            .setTlsSocketStrategy((TlsSocketStrategy) tlsStrategy)
//                            .build();
//
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setConnectionManager(cm)
//                    .build();
//
//            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//
//            return new RestTemplate(factory);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
