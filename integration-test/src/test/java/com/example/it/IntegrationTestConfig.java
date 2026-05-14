package com.example.it;

import com.example.bar.RedisReaderService;
import com.example.bar.S3ReaderService;
import com.example.bar.StockAlertPublisher;
import com.example.carnival.CarnivalRedisService;
import com.example.carnival.CarnivalService;
import com.example.carnival.StockAlertListener;
import com.example.common.S3Config;
import com.example.common.S3Properties;
import com.example.foo.RedisWriterService;
import com.example.foo.S3WriterService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*
 * Minimal Spring context for the cross-module integration test.
 *
 * Imports the full S3 + Redis + Kafka service chain explicitly rather than
 * relying on component scan so the context is predictable and fast.
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
@ImportAutoConfiguration({RedisAutoConfiguration.class, KafkaAutoConfiguration.class})
@Import({
        S3Config.class,
        S3WriterService.class, CarnivalService.class, S3ReaderService.class,
        RedisWriterService.class, CarnivalRedisService.class, RedisReaderService.class,
        StockAlertPublisher.class, StockAlertListener.class
})
public class IntegrationTestConfig {}
