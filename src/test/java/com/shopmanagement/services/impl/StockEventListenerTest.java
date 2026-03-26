package com.shopmanagement.services.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopmanagement.event.StockReconciliationEvent;

@ExtendWith(MockitoExtension.class)
class StockEventListenerTest {

    @Mock
    private StockReconciliationService stockReconciliationService;

    @InjectMocks
    private StockEventListener stockEventListener;

    @Test
    void shouldDelegateToReconciliationService() {
        stockEventListener.handleStockReconciliation(new StockReconciliationEvent(50L));

        verify(stockReconciliationService).reconcileOrder(50L);
        verify(stockReconciliationService, never()).markOrderFailed(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldMarkOrderFailedWhenReconciliationThrows() {
        doThrow(new RuntimeException("Insufficient stock")).when(stockReconciliationService).reconcileOrder(51L);

        stockEventListener.handleStockReconciliation(new StockReconciliationEvent(51L));

        verify(stockReconciliationService).markOrderFailed(51L, "Insufficient stock");
    }
}
