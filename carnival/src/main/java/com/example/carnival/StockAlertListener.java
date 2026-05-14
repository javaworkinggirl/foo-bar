package com.example.carnival;

import com.example.common.StockAlert;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("PMD.AtLeastOneConstructor")
@Service
public class StockAlertListener {

    private final ConcurrentMap<String, StockAlert> latestByItem = new ConcurrentHashMap<>();

    @KafkaListener(topics = StockAlert.TOPIC, groupId = "carnival")
    public void onAlert(final StockAlert alert) {
        latestByItem.put(alert.itemName(), alert);
    }

    public StockAlert getLatest(final String itemName) {
        return latestByItem.get(itemName);
    }
}
