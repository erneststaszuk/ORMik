package com.example.ormik.infrastructure.busmock

import com.example.ormik.outbox.OutboxMessage

interface EventHandler {
    val subscriptionName: SubscriptionName

    fun handle(message: OutboxMessage)
}
