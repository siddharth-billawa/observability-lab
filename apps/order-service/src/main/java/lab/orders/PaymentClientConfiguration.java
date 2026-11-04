package lab.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
class PaymentClientConfiguration {
    @Bean
    RestClient paymentRestClient(RestClient.Builder builder, @Value("${payment.base-url}") String paymentUrl) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(7));
        return builder.baseUrl(paymentUrl).requestFactory(factory).build();
    }
}
