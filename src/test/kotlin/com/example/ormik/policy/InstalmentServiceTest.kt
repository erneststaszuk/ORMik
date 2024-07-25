package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldHaveSize
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class InstalmentServiceTest : IntegrationTest {

  @Autowired
  lateinit var policyService: PolicyService

  @Autowired
  lateinit var policyWalletRepository: PolicyWalletRepository

  @Test
  fun `create policy with monthly instalments`() {
    // given
    val policy = Fixture.policy()

    // when
    policyService.createPolicy(policy, PaymentInterval.MONTHLY)

    // then
    val instalments = policyWalletRepository.findByPoliciesIdsContains(policy.id.toPolicyRef())
    instalments.instalments shouldHaveSize 12
  }
}
