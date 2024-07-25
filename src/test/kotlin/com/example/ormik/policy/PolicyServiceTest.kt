package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired

class PolicyServiceTest : IntegrationTest {

    @Autowired
    lateinit var policyService: PolicyService

    @Autowired
    lateinit var policyWalletRepository: PolicyWalletRepository

    @ParameterizedTest
    @EnumSource
    fun `create policy with monthly instalments`(case: CreatePolicyWithInstalmentsTestCase) {
        // given
        val policy = Fixture.policy()

        // when
        policyService.createPolicy(policy, case.paymentInterval)

        // then
        val policyInstalments = policyWalletRepository.findByPoliciesIdsContains(policy.id.toPolicyRef())
        policyInstalments.instalments shouldHaveSize case.expectedInstalments
        policyInstalments.instalments.sumOf { it.amount } shouldBeEqualTo policy.premium
    }

    enum class CreatePolicyWithInstalmentsTestCase(
        val paymentInterval: PaymentInterval,
        val expectedInstalments: Int
    ) {
        `annual payment`(PaymentInterval.ANNUAL, 1),
        `monthly payment`(PaymentInterval.MONTHLY, 12),
    }

}
