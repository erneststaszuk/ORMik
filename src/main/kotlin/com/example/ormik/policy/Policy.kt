package com.example.ormik.policy

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import java.math.BigDecimal
import java.time.LocalDate

@Table
data class Policy(
  @Id val id: Long? = null,
  val holderParty: String,
  val insuredParty: String,
  val beneficiaryParty: String,
  val fromDate: LocalDate,
  val thruDate: LocalDate,
  val mainSumInsured: BigDecimal,
  val hsdr17SumInsured: BigDecimal?,
  val ccb17SumInsured: BigDecimal?,
  val ccbh17SumInsured: BigDecimal?,
  val premium: BigDecimal,
)

interface PolicyRepository: CrudRepository<Policy, Long>
