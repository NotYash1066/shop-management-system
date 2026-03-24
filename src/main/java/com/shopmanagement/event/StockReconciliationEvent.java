package com.shopmanagement.event;

import lombok.Getter;

@Getter
public class StockReconciliationEvent {
    private final Long orderId;

    public StockReconciliationEvent(Long orderId) {
        this.orderId = orderId;
    }
}
