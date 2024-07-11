package com.example.ormik.policy

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

@Table
data class Instalment(
    @Id val id: UUID,
    val policyId: UUID,
    val amount: BigDecimal,
    val due: LocalDate,
    val isPaid: Boolean,
)

@Table
data class PolicyInstalments(
    @Id val policyId: UUID,
    @MappedCollection(idColumn = "policy_id", keyColumn = "seq_index")
    val instalments: List<Instalment>,
    @Version val version: Long = 0L,
)

interface InstalmentsRepository: CrudRepository<PolicyInstalments, UUID> {
    fun findByPolicyId(policyId: UUID): PolicyInstalments
}

@Service
class InstalmentService(private val repository: InstalmentsRepository) {
    fun createInstalments(policy: Policy, paymentInterval: PaymentInterval) {
        val policyInstalments = when(paymentInterval) {
            PaymentInterval.ANNUAL -> createSingleInstalment(policy)
            PaymentInterval.MONTHLY -> createMonthlyInstallments(policy)
        }
        repository.save(PolicyInstalments(policy.id, policyInstalments))
    }

    private fun createSingleInstalment(policy: Policy): List<Instalment> =
        listOf(createInstalment(policy, policy.premium, policy.fromDate.minusDays(1)))

    private fun createInstalment(policy: Policy, amount: BigDecimal, due: LocalDate) = Instalment(
        id = UUID.randomUUID(),
        policyId = policy.id,
        amount = amount,
        due = due,
        isPaid = false
    )

    private fun createMonthlyInstallments(policy: Policy): List<Instalment> {
        val monthlyPremium = policy.premium.divide(monthsInYear, 2, RoundingMode.DOWN)
        val firstPremium = policy.premium - monthlyPremium.multiply(monthsInYear - ONE)
        val firstDue = policy.fromDate.minusDays(1)

        return listOf(
            createInstalment(policy, firstPremium, firstDue),
            *(1L..11L)
                .map { monthsAfterFirstInstalment ->
                    createInstalment(policy, monthlyPremium, firstDue.plusMonths(monthsAfterFirstInstalment))
                }.toTypedArray()
        )
    }

    fun payAmount(amount: BigDecimal, currentDate: LocalDate) {
        TODO()
    }

    companion object {
        val monthsInYear = BigDecimal("12")
    }
}


enum class PaymentInterval {
    ANNUAL, MONTHLY
}
