package com.example.ormik.policy

import com.example.ormik.infrastructure.Transactionally
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Table
data class Policy(
  @Id val id: UUID,
  @Embedded(onEmpty = OnEmpty.USE_EMPTY) val parties: PolicyParties,
  val fromDate: LocalDate,
  val thruDate: LocalDate,
  val mainSumInsured: BigDecimal,
  val hsdr17SumInsured: BigDecimal?,
  val ccb17SumInsured: BigDecimal?,
  val ccbh17SumInsured: BigDecimal?,
  val premium: BigDecimal,
  @Version val version: Long = 0L,
)

data class PolicyParties (
  val holderParty: String,
  val insuredParty: String,
  val beneficiaryParty: String,
)

interface PolicyRepository: CrudRepository<Policy, Long>

@Repository
interface PolicyReportsRepository: org.springframework.data.repository.Repository<Policy, Long> {

  @Query("SELECT sum(premium) FROM policy")
  @Transactional(readOnly = true)
  fun queryPremiumsSum(): BigDecimal
}

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
