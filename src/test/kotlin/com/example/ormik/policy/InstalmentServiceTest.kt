package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

class InstalmentServiceTest : IntegrationTest {

  @Autowired
  lateinit var policyService: PolicyService

  @Autowired
  lateinit var policyRepository: PolicyRepository

  @Autowired
  lateinit var installmentListRepository: InstallmentListRepository

  @Autowired
  lateinit var instalmentService: InstalmentService

  @Test
  fun `create policy with monthly instalments`() {
    // given
    val policy = Fixture.policy()

    // when
    policyService.createPolicy(policy, PaymentInterval.MONTHLY)

    // then
    val instalmentList = installmentListRepository.findById(installmentListRepository.findInstallmentListIdByPolicy(policy.id)).get()
    instalmentList.installments shouldHaveSize 12
  }

  @Test
  fun `should pay for all policies for 2 months`() {
    // given
    val policyA = Fixture.policy()
    val policyB = Fixture.policy()
    val policies = setOf(policyA, policyB)
    policyService.createPoliciesWallet(policies, PaymentInterval.MONTHLY)

    // when
    val instalmentList = installmentListRepository.findById(installmentListRepository.findInstallmentListIdByPolicy(policies.first().id)).get()
    instalmentService.payAmount(instalmentList, BigDecimal("2140.15") * BigDecimal(4), LocalDate.of(2024, 6, 16))

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
    val policyA = Fixture.policy()
    val policyB = Fixture.policy()
    val policies = setOf(policyA, policyB)
    policyService.createPoliciesWallet(policies, PaymentInterval.MONTHLY)

    // when
    val instalmentList = installmentListRepository.findById(installmentListRepository.findInstallmentListIdByPolicy(policies.first().id)).get()
    instalmentService.payAmount(instalmentList, BigDecimal("2140.15") * BigDecimal(3), LocalDate.of(2024, 6, 16))

    // then
    val paidInstallmentList = installmentListRepository.findById(instalmentList.id).get()
    paidInstallmentList.saldo shouldBeEqualTo BigDecimal("2140.15")
    val paidPolicyA = policyRepository.findById(policyA.id).get()
    val paidPolicyB = policyRepository.findById(policyB.id).get()
    paidPolicyA.isPaidTo shouldBeEqualTo LocalDate.of(2024, 6, 16)
    paidPolicyB.isPaidTo shouldBeEqualTo LocalDate.of(2024, 6, 16)
  }
}
