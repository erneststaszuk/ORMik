package com.example.ormik.infrastructure.busmock

import com.example.ormik.outbox.OutboxMessage
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

typealias TopicName = String
typealias SubscriptionName = String

@Configuration
@EnableConfigurationProperties(
    BusProperties::class,
)
class BusConfig

@Service
class Bus(busProperties: BusProperties) {

    private val topicsToSubscriptions: Map<TopicName, List<SubscriptionName>> =
        busProperties.topics
            .map { it.name to it.subscriptions }
            .toMap()

    private val subscriptions = ConcurrentHashMap<SubscriptionName, Queue<OutboxMessage>>()

    fun publish(vararg outboxMessages: OutboxMessage) {
        outboxMessages.forEach { message ->
            val topicName = message.topic
            val targetSubscriptions = topicsToSubscriptions[topicName] ?: emptyList()
            targetSubscriptions.forEach { subscription ->
                val queue = subscriptions.computeIfAbsent(subscription) { _ ->
                     LinkedBlockingQueue(5)
                }
                queue.add(message)
            }
        }
    }

    fun pullMessages(subscription: SubscriptionName) =
        subscriptions[subscription]?.remove()

    fun purge() {
        subscriptions.clear()
    }
}

@ConfigurationProperties("bus")
data class BusProperties(
    val topics: List<TopicConfig>
)

data class TopicConfig(
    val name: TopicName,
    val subscriptions: List<SubscriptionName>
)
