package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test

class PolicyServiceTest : IntegrationTest {

  @Autowired
  lateinit var policyService: PolicyService

  @Autowired
  lateinit var instalmentRepository: InstalmentRepository

  @Test
  fun `create policy with monthly instalments`() {
    // given
    val policy = Fixture.policy()

    // when
    val saved = policyService.createPolicy(policy, PaymentInterval.MONTHLY)

    // then
    val instalments = instalmentRepository.findAllByPolicyId(policy.id)
    instalments shouldHaveSize 12
    instalments.sumOf { it.amount } shouldBeEqualTo policy.premium
  }
}
