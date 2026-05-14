package com.example.bar;

import com.example.common.StockAlert;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockAlertPublisher {

    private final KafkaTemplate<String, StockAlert> kafkaTemplate;

    public StockAlertPublisher(final KafkaTemplate<String, StockAlert> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(final StockAlert alert) {
        kafkaTemplate.send(StockAlert.TOPIC, alert.itemName(), alert);
    }
}
