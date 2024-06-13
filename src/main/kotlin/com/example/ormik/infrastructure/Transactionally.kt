package com.example.ormik.infrastructure

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

interface Transactionally {
  fun <T> call(block: () -> T): T

  operator fun <T> invoke(block: () -> T): T = call(block)
}

@Component
class SpringTransactionally(platformTransactionManager: PlatformTransactionManager) : Transactionally {

  private val transactionTemplate = TransactionTemplate(platformTransactionManager)

  //blocking spring transaction
  override fun <T> call(block: () -> T) = transactionTemplate.execute { block() }!!
}