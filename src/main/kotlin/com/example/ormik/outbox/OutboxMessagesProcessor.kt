package com.example.ormik.outbox

import com.example.ormik.infrastructure.busmock.Bus
import org.springframework.stereotype.Service

@Service
class OutboxMessagesProcessor(bus: Bus) {
    fun run() {
        TODO()
    }
}