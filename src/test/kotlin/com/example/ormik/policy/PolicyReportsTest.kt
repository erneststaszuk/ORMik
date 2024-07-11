package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainSame
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import kotlin.test.Test

class PolicyReportsTest: IntegrationTest {

    @Autowired
    lateinit var policyRepository: PolicyRepository

    @Autowired
    lateinit var policyReportsRepository: PolicyReportsRepository

    @Test
    fun `summarize premiums`() {
        // given
        val policy1 = Fixture.policy()
        val policy2 = Fixture.policy()
        policyRepository.saveAll(listOf(policy1, policy2))

        // when
        val premiumsSum = policyReportsRepository.queryPremiumsSum()

        // then
        premiumsSum shouldBeEqualTo (policy1.premium + policy2.premium)
    }

    @Test
    fun `find active policies`() {
        // given
        val policy1 = Fixture.policy(fromDate = LocalDate.of(2024, 6, 1), thruDate = LocalDate.of(2025, 5, 31))
        val policy2 = Fixture.policy(fromDate = LocalDate.of(2024, 7, 1), thruDate = LocalDate.of(2025, 6, 30))
        val policy3 = Fixture.policy(fromDate = LocalDate.of(2023, 3, 1), thruDate = LocalDate.of(2024, 4, 29))
        policyRepository.saveAll(listOf(policy1, policy2, policy3))

        // when
        val policies1 = policyReportsRepository.findPoliciesActiveAtDate(LocalDate.of(2024, 7, 11))

        // then
        policies1 shouldContainSame setOf(policy1.nextVersion(), policy2.nextVersion())

        // when
        val policies2 = policyReportsRepository.findPoliciesActiveAtDate(LocalDate.of(2023, 7, 11))

        // then
        policies2 shouldContainSame setOf(policy3.nextVersion())
    }
}

internal fun Policy.nextVersion() =
    this.copy(version = this.version + 1)
