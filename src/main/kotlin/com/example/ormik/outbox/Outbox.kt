package com.example.ormik.outbox

import com.example.ormik.policy.Policy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Table
data class OutboxMessage(
    @Id val id: UUID,
    val topic: String,
    val type: String,
    val payload: ByteArray,
    @CreatedDate val created_at: Instant? = null,
)

interface OutboxMessageRepository : CrudRepository<Policy, UUID>
