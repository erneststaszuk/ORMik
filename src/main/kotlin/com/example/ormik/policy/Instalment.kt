package com.example.ormik.policy

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.jdbc.core.mapping.AggregateReference
import org.springframework.data.jdbc.core.mapping.AggregateReference.IdOnlyAggregateReference
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

@Table
data class Instalment(
    @Id val id: UUID,
    val amount: BigDecimal,
    val due: LocalDate,
    val isPaid: Boolean,
)

data class InstallmentList(
    val policies: Set<PolicyRef>,
    @MappedCollection(idColumn = "installment_list_id", keyColumn = "seq_index")
    val installments: List<Instalment>,
)

data class PolicyRef(
    val policyId: UUID,
)

interface InstallmentListRepository: CrudRepository<InstallmentList, UUID> {
    fun findByPolicyId(policyId: UUID): InstallmentList
}

@Service
class InstalmentService(private val repository: InstallmentListRepository) {
    fun createInstalments(policies: Set<Policy>, paymentInterval: PaymentInterval) {
        if (policies.map { it.fromDate }.toSet().size != 1) {
            throw PoliciesHaveDifferingStartDates(policies)
        }
        if (policies.map { it.thruDate }.toSet().size != 1) {
            throw PoliciesHaveDifferingEndDates(policies)
        }

        val policyInstalments = when (paymentInterval) {
            PaymentInterval.ANNUAL -> createSingleInstalment(policies)
            PaymentInterval.MONTHLY -> createMonthlyInstallments(policies)
        }
        val policiesRefs = policies.map { PolicyRef(it.id) }.toSet()
        repository.save(InstallmentList(policiesRefs, policyInstalments))
    }

    private fun createSingleInstalment(policies: Set<Policy>): List<Instalment> =
        listOf(
            createInstalment(
                policyWalletId = UUID.randomUUID(),
                amount = policies.sumOf { it.premium },
                due = policies.map { it.fromDate }.toSet().single().minusDays(1)
            )
        )

    private fun createInstalment(policyWalletId: UUID, amount: BigDecimal, due: LocalDate) = Instalment(
        id = UUID.randomUUID(),
        policies = policyWalletId,
        amount = amount,
        due = due,
        isPaid = false
    )

    private fun createMonthlyInstallments(policies: Set<Policy>): List<Instalment> {
        val premiumsSum = policies.sumOf { it.premium }
        val monthlyPremium = premiumsSum.divide(monthsInYear, 2, RoundingMode.DOWN)
        val firstPremium = premiumsSum - monthlyPremium.multiply(monthsInYear - ONE)
        val firstDue = policies.map { it.fromDate }.toSet().single().minusDays(1)
        val policyWalletId = UUID.randomUUID()

        return listOf(
            createInstalment(policyWalletId, firstPremium, firstDue),
            *(1L..11L)
                .map { monthsAfterFirstInstalment ->
                    createInstalment(policyWalletId, monthlyPremium, firstDue.plusMonths(monthsAfterFirstInstalment))
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

data class PoliciesHaveDifferingStartDates(val policies: Set<Policy>) : RuntimeException()

data class PoliciesHaveDifferingEndDates(val policies: Set<Policy>) : RuntimeException()

enum class PaymentInterval {
    ANNUAL, MONTHLY
}

fun UUID.toPolicyRef() =
    IdOnlyAggregateReference<Policy, UUID>(this)
