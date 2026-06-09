package com.example.cafeordersystem.global.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private Integer retries;

    @Value("${spring.kafka.producer.properties.enable.idempotence:true}")
    private Boolean enableIdempotence;

    @Value("${spring.kafka.properties.security.protocol:}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 안전한 발행 설정
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        /*
         * Confluent Cloud 접속용 SASL 설정
         *
         * 직접 ProducerFactory를 만들면 Spring Boot 자동 Kafka 설정이 그대로 들어가지 않는다.
         * 그래서 EB 환경변수로 받은 SASL 설정을 props에 직접 넣어야 한다.
         */
        putIfNotBlank(
                props,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                securityProtocol
        );

        putIfNotBlank(
                props,
                SaslConfigs.SASL_MECHANISM,
                saslMechanism
        );

        putIfNotBlank(
                props,
                SaslConfigs.SASL_JAAS_CONFIG,
                saslJaasConfig
        );

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private void putIfNotBlank(
            Map<String, Object> props,
            String key,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            props.put(key, value);
        }
    }
}