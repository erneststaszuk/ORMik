package com.example.ormik.policy

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.sql.In
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.MathContext
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
    @Version val version: Long = 0L,
)

interface InstalmentRepository: CrudRepository<Instalment, UUID> {
    fun findAllByPolicyId(policyId: UUID): List<Instalment>
}

@Service
class InstalmentService(private val repository: InstalmentRepository) {
    fun createInstalments(policy: Policy, paymentInterval: PaymentInterval) {
        val policyInstalments = when(paymentInterval) {
            PaymentInterval.ANNUAL -> listOf(
                createInstalment(policy, policy.premium, policy.fromDate.minusDays(1))
            )
            PaymentInterval.MONTHLY -> createMonthlyInstallments(policy)
        }
        repository.saveAll(policyInstalments)
    }

    private fun createSingleInstalment(policy: Policy) =
        createInstalment(policy, policy.premium, policy.fromDate.minusDays(1))

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

    companion object {
        val monthsInYear = BigDecimal("12")
    }
}


enum class PaymentInterval {
    ANNUAL, MONTHLY
}
