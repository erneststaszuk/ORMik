package com.example.ormik.policy

import com.example.ormik.infrastructure.Transactionally
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class PaymentService(
    private val instalmentService: InstalmentService,
    private val policyService: PolicyService,
    private val transactionally: Transactionally
) {
    fun payAmount(installmentListId: UUID, amount: BigDecimal, currentDate: LocalDate) {
        transactionally {
            val instalmentList = instalmentService.payAmount(installmentListId, amount, currentDate)
            val payPoliciesTo = instalmentList.isPaidTo()

            payPoliciesFromList(payPoliciesTo, instalmentList)
        }
    }

    private fun payPoliciesFromList(payPoliciesTo: LocalDate?, instalmentList: InstallmentList) {
        if (payPoliciesTo != null) {
            val policiesIds = instalmentList.policies.map { it.policy }.toSet()
            policyService.payPoliciesUpTo(policiesIds, payPoliciesTo)
        }
    }
}
