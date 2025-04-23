package br.com.vnrg.mtls.test;


import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;


@Slf4j
// @Configuration
public class RestClient2Config {

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
    public RestTemplate restTemplate() {
        try {
            SSLContext sslContext = configureSSLContext();
            return new RestTemplate(createRequestFactory(sslContext));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    private SSLContext configureSSLContext() {
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

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, keyStorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(ts);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return sslContext;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    private ClientHttpRequestFactory createRequestFactory(SSLContext sslContext) {
        return new CustomRequestFactory(sslContext);
    }

    private static class CustomRequestFactory extends SimpleClientHttpRequestFactory {

        private final SSLContext sslContext;

        public CustomRequestFactory(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws IOException {
            if (connection instanceof javax.net.ssl.HttpsURLConnection) {
                ((javax.net.ssl.HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
            }
            super.prepareConnection(connection, httpMethod);
        }
    }

}
