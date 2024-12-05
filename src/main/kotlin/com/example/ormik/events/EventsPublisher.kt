package com.example.ormik.events

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

class EventsPublisher {

}

@Table
data class DomainEvent(
    @Id val id: UUID,
    val type: EventType,
    val payload: String
)

enum class EventType {

}
