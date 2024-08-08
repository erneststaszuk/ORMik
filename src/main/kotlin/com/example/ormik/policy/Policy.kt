package com.example.ormik.policy

import com.example.ormik.infrastructure.Transactionally
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream

@Table
data class Policy(
    @Id val id: UUID,
    @Embedded(onEmpty = OnEmpty.USE_EMPTY) val parties: PolicyParties,
    val fromDate: LocalDate,
    val thruDate: LocalDate,
    @MappedCollection(idColumn = "policy_id", keyColumn = "seq_order")
    val selectedRisks: List<SelectedRisk>,
    val premium: BigDecimal,
    val isActive: Boolean = false,
    val isPaidTo: LocalDate? = null,
    @Version val version: Long = 0L,
) {
    init {
        require(thruDate > fromDate)
    }

    fun payTo(newPaidTo: LocalDate): Policy {
        if (isPaidTo != null && newPaidTo < isPaidTo)
            throw PolicyAlreadyIsPaidFurther(id, isPaidTo, newPaidTo)

        return copy(isPaidTo = newPaidTo)
    }
}

data class PolicyAlreadyIsPaidFurther(
    val policyId: UUID,
    val isPaidTo: LocalDate,
    val requestedPaidTo: LocalDate
) : RuntimeException("""{"error": "PolicyAlreadyIsPaidFurther", "data": {"policyId": "$policyId", "isPaidTo": "$isPaidTo", "newPaidTo": "$requestedPaidTo"}""")

data class PolicyParties(
    val holderParty: String,
    val insuredParty: String,
    val beneficiaryParty: String,
)

@Table
data class SelectedRisk(
    val riskCode: String,
    val sumInsured: BigDecimal
)

interface PolicyRepository : CrudRepository<Policy, UUID>

@Repository
interface PolicyReportsRepository : org.springframework.data.repository.Repository<Policy, Long> {

    @Query("SELECT sum(premium) FROM policy")
    fun queryPremiumsSum(): BigDecimal

    @Query("SELECT * FROM policy WHERE from_date <= :atDate AND :atDate <= thru_date")
    fun findPoliciesActiveAtDate(atDate: LocalDate): Stream<Policy>
}

@Service
class PolicyService(
    private val repository: PolicyRepository,
    private val instalmentService: InstalmentService,
    private val transactionally: Transactionally,
) {
    fun getPolicy(policyId: UUID): Policy =
        repository.findById(policyId).get()

    fun createPolicy(policy: Policy, paymentInterval: PaymentInterval = PaymentInterval.ANNUAL): Policy =
        createPoliciesWallet(setOf(policy), paymentInterval)
            .single()

    fun createPoliciesWallet(
        policies: Set<Policy>,
        paymentInterval: PaymentInterval = PaymentInterval.ANNUAL
    ): Set<Policy> =
        transactionally {
            repository.saveAll(policies).toSet().also {
                instalmentService.createInstalments(it, paymentInterval)
            }
        }

    fun payPoliciesUpTo(policiesIds: Set<UUID>, payTo: LocalDate): Set<Policy> =
        transactionally {
            val policies = repository.findAllById(policiesIds)
            val changedPolicies = policies.map { it.payTo(payTo) }

            repository.saveAll(changedPolicies).toSet().also {
                it.forEach {
                    log.info(it.toString())
                }
            }
        }

    companion object {
        val log: Logger = LoggerFactory.getLogger(PolicyService::class.java)
    }
}

