package com.example.ormik.policy

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.jdbc.repository.query.Query
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
    val amount: BigDecimal,
    val due: LocalDate,
    val isPaid: Boolean,
) {
    fun paymentIsAcceptableRegardingDueDate(currentDate: LocalDate): Boolean =
        due.plusDays(14) >= currentDate

    fun payOff(currentDate: LocalDate): Instalment {
        if (isPaid)
            throw InstalmentIsAlreadyPaidOff(this)

        if (!paymentIsAcceptableRegardingDueDate(currentDate))
            throw InstalmentIsOverduedPastGracePeriod(this, currentDate)

        return copy(isPaid = true)
    }
}

data class InstallmentList(
    @Id val id: UUID = UUID.randomUUID(),
    val policies: Set<PolicyRef>,
    @MappedCollection(idColumn = "installment_list_id", keyColumn = "seq_index")
    val installments: List<Instalment>,
    val saldo: BigDecimal = BigDecimal("0.00"),
    @Version val version: Long = 0L,
) {
    fun isPaidTo(): LocalDate? =
        installments.filter { it.isPaid }
            .sortedBy { it.due }
            .lastOrNull()
            ?.due

    fun payAmount(amount: BigDecimal, currentDate: LocalDate): InstallmentList {
        require(amount > BigDecimal.ZERO && amount.scale() == 2)

        val processedInstallments = mutableListOf<Instalment>()
        var currentSaldo = saldo + amount

        installments.forEach { installment ->
            if (!installment.isPaid
                && currentSaldo >= installment.amount
                && installment.paymentIsAcceptableRegardingDueDate(currentDate)
            ) {
                val paid = installment.payOff(currentDate)
                currentSaldo -= installment.amount
                processedInstallments.add(paid)
            } else {
                processedInstallments.add(installment)
            }
        }

        return copy(installments = processedInstallments, saldo = currentSaldo)
    }
}

data class PolicyRef(
    val policy: UUID,
)

interface InstallmentListRepository : CrudRepository<InstallmentList, UUID> {
    //fun getInstallmentListByInstallmentListId(installmentListId: UUID): InstallmentList
    @Query("SELECT installment_list FROM policy_ref WHERE policy = :policyId")
    fun findInstallmentListIdByPolicy(policyId: UUID): UUID
}

@Service
class InstalmentService(
    private val repository: InstallmentListRepository,
) {
    fun createInstalments(policies: Set<Policy>, paymentInterval: PaymentInterval) {
        if (policies.map { it.fromDate }.toSet().size != 1) {
            throw PoliciesHaveDifferingStartDates(policies)
        }
        if (policies.map { it.thruDate }.toSet().size != 1) {
            throw PoliciesHaveDifferingEndDates(policies)
        }

        val instalments = when (paymentInterval) {
            PaymentInterval.ANNUAL -> createSingleInstalment(policies)
            PaymentInterval.MONTHLY -> createMonthlyInstallments(policies)
        }
        val policiesRefs = policies.map { PolicyRef(it.id) }.toSet()
        repository.save(InstallmentList(policies = policiesRefs, installments = instalments))
    }

    private fun createSingleInstalment(policies: Set<Policy>): List<Instalment> =
        listOf(
            createInstalment(
                amount = policies.sumOf { it.premium },
                due = policies.map { it.fromDate }.toSet().single().minusDays(1)
            )
        )

    private fun createInstalment(amount: BigDecimal, due: LocalDate) = Instalment(
        id = UUID.randomUUID(),
        amount = amount,
        due = due,
        isPaid = false
    )

    private fun createMonthlyInstallments(policies: Set<Policy>): List<Instalment> {
        val premiumsSum = policies.sumOf { it.premium }
        val monthlyPremium = premiumsSum.divide(monthsInYear, 2, RoundingMode.DOWN)
        val firstPremium = premiumsSum - monthlyPremium.multiply(monthsInYear - ONE)
        val firstDue = policies.map { it.fromDate }.toSet().single().minusDays(1)

        return listOf(
            createInstalment(firstPremium, firstDue),
            *(1L..11L)
                .map { monthsAfterFirstInstalment ->
                    createInstalment(monthlyPremium, firstDue.plusMonths(monthsAfterFirstInstalment))
                }.toTypedArray()
        )
    }

    fun payAmount(installmentListId: UUID, amount: BigDecimal, currentDate: LocalDate): InstallmentList {
        val changedInstallmentList = repository.findById(installmentListId)
            .get()
            .payAmount(amount, currentDate)
        return repository.save(changedInstallmentList)
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

data class InstalmentIsAlreadyPaidOff(val instalment: Instalment) :
    RuntimeException("Installment is already paid off $instalment")

data class InstalmentIsOverduedPastGracePeriod(val instalment: Instalment, val currentDate: LocalDate) :
    RuntimeException("At date $currentDate installment $instalment is overdue past grace period.")
