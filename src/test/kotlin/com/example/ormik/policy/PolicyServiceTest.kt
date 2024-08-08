package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import kotlin.test.Test

class PolicyServiceTest : IntegrationTest {

    @Autowired
    lateinit var policyService: PolicyService

    @Autowired
    lateinit var installmentListRepository: InstallmentListRepository

    @ParameterizedTest
    @EnumSource
    fun `create policy with monthly instalments`(case: CreatePolicyWithInstalmentsTestCase) {
        // given
        val policy = Fixture.policy()

        // when
        policyService.createPolicy(policy, case.paymentInterval)

        // then
        val policyInstalments = installmentListRepository.findById(
            installmentListRepository.findInstallmentListIdByPolicy(policy.id)
        ).get()
        policyInstalments.installments shouldHaveSize case.expectedInstalments
        policyInstalments.installments.sumOf { it.amount } shouldBeEqualTo policy.premium
    }

    enum class CreatePolicyWithInstalmentsTestCase(
        val paymentInterval: PaymentInterval,
        val expectedInstalments: Int
    ) {
        `annual payment`(PaymentInterval.ANNUAL, 1),
        `monthly payment`(PaymentInterval.MONTHLY, 12),
    }

    @Test
    fun `cannot pay policy before it is already paid to`() {
        // given
        val novemberDate = LocalDate.of(2024, 11, 17)
        val juneDate = LocalDate.of(2024, 6, 17)
        val policy = Fixture.policy()
        val v1 = policyService.createPolicy(policy)

        // when
        val paidToNovember = policyService.payPoliciesUpTo(setOf(v1.id), novemberDate).single()

        // then
        paidToNovember.isPaidTo shouldBeEqualTo novemberDate
        paidToNovember.version shouldBeEqualTo 2

        // expect
        invoking {
            policyService.payPoliciesUpTo(setOf(v1.id), juneDate)
        } shouldThrow PolicyAlreadyIsPaidFurther(v1.id, novemberDate, juneDate)

        // then
        policyService.getPolicy(v1.id).isPaidTo shouldBeEqualTo novemberDate
    }
}
