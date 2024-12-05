package com.example.ormik.shop

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class DeliveryCostCalculator {
    fun calculateDeliveryCost(itemsPrice: BigDecimal): BigDecimal =
        if(itemsPrice < BigDecimal("200.00"))
}