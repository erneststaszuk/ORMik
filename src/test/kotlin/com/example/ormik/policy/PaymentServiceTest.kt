package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

class PaymentServiceTest: IntegrationTest {

    @Autowired
    lateinit var policyService: PolicyService

    @Autowired
    lateinit var policyRepository: PolicyRepository

    @Autowired
    lateinit var installmentListRepository: InstallmentListRepository

    @Autowired
    lateinit var paymentService: PaymentService

    @Test
    fun `should pay for all policies for 2 months`() {
        // given
        val monthlyPremium = BigDecimal("350.00")
        val policyA = Fixture.policy(premium = monthlyPremium.multiply(BigDecimal(12)))
        val policyB = Fixture.policy(premium = monthlyPremium.multiply(BigDecimal(12)))
        val policies = setOf(policyA, policyB)
        policyService.createPoliciesWallet(policies, PaymentInterval.MONTHLY)

        // when
        val instalmentList = installmentListRepository.findById(installmentListRepository.findInstallmentListIdByPolicy(policies.first().id)).get()
        paymentService.payAmount(
            installmentListId = instalmentList.id,
            amount = monthlyPremium * BigDecimal(4),
            currentDate = LocalDate.of(2024, 6, 16)
        )

        // then
        val paidInstallmentList = installmentListRepository.findById(instalmentList.id).get()
        paidInstallmentList.saldo shouldBeEqualTo BigDecimal("0.00")
        val paidPolicyA = policyRepository.findById(policyA.id).get()
        val paidPolicyB = policyRepository.findById(policyB.id).get()
        paidPolicyA.isPaidTo shouldBeEqualTo LocalDate.of(2024, 7, 16)
        paidPolicyB.isPaidTo shouldBeEqualTo LocalDate.of(2024, 7, 16)
    }

    @Test
    fun `should pay for all policies and left saldo`() {
        // given
        val monthlyPremium = BigDecimal("350.00")
        val policyA = Fixture.policy(premium = monthlyPremium.multiply(BigDecimal(12)))
        val policyB = Fixture.policy(premium = monthlyPremium.multiply(BigDecimal(12)))
        val policies = setOf(policyA, policyB)
        policyService.createPoliciesWallet(policies, PaymentInterval.MONTHLY)

        // when
        val instalmentList = installmentListRepository.findById(installmentListRepository.findInstallmentListIdByPolicy(policies.first().id)).get()
        paymentService.payAmount(
            installmentListId = instalmentList.id,
            amount = monthlyPremium * BigDecimal(3),
            currentDate = LocalDate.of(2024, 6, 16)
        )

        // then
        val paidInstallmentList = installmentListRepository.findById(instalmentList.id).get()
        paidInstallmentList.saldo shouldBeEqualTo monthlyPremium
        val paidPolicyA = policyRepository.findById(policyA.id).get()
        val paidPolicyB = policyRepository.findById(policyB.id).get()
        paidPolicyA.isPaidTo shouldBeEqualTo LocalDate.of(2024, 6, 16)
        paidPolicyB.isPaidTo shouldBeEqualTo LocalDate.of(2024, 6, 16)
    }
}