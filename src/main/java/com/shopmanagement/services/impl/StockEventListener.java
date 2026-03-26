package com.shopmanagement.services.impl;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.shopmanagement.event.StockReconciliationEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockEventListener {

    private final StockReconciliationService stockReconciliationService;

    public StockEventListener(StockReconciliationService stockReconciliationService) {
        this.stockReconciliationService = stockReconciliationService;
    }

    @Async("stockReconciliationExecutor")
    @EventListener
    public void handleStockReconciliation(StockReconciliationEvent event) {
        long startedAt = System.nanoTime();
        log.info("Processing stock reconciliation for order ID: {}", event.getOrderId());

        try {
            stockReconciliationService.reconcileOrder(event.getOrderId());
            log.info("Stock reconciliation completed for order ID: {} in {} ms",
                    event.getOrderId(),
                    (System.nanoTime() - startedAt) / 1_000_000);
        } catch (Exception e) {
            log.error("Stock reconciliation failed for order ID: {}. Error: {}", event.getOrderId(), e.getMessage());
            stockReconciliationService.markOrderFailed(event.getOrderId(), e.getMessage());
        }
    }
}
