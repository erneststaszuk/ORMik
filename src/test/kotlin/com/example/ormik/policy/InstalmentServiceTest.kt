package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldHaveSize
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class InstalmentServiceTest : IntegrationTest {

  @Autowired
  lateinit var policyService: PolicyService

  @Autowired
  lateinit var installmentListRepository: InstallmentListRepository

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
}
