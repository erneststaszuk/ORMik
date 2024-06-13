package com.example.ormik.policy

import com.example.ormik.infrastructure.Transactionally
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
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

@Service
class PolicyService(
  private val repository: PolicyRepository,
  private val transactionally: Transactionally,
){
  fun saveAll(policies: Iterable<Policy>) {
    transactionally {
      policies.forEach {
        repository.save(it)
      }
    }
  }
}
