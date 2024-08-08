package com.example.ormik.outbox

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledOutboxMessagesProcessor(
  private val outboxMessagesProcessor: OutboxMessagesProcessor
) {

  @Scheduled(fixedDelayString = "\${outbox-messages-processor.delay:PT1S}")
  fun run() {
    outboxMessagesProcessor.run()
  }
}
