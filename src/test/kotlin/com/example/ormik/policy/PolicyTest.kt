package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

internal typealias bd = BigDecimal

class PolicyTest : IntegrationTest {

  @Autowired
  lateinit var policyRepository: PolicyRepository

  @Test
  fun `saved policy has id`() {
    // given
    val policy = Policy(
      holderParty = "Pan/Pani Holder",
      insuredParty = "Pan/Pani Insured",
      beneficiaryParty = "Pan/Pani Beneficiary",
      fromDate = LocalDate.of(2024, 6, 17),
      thruDate = LocalDate.of(2025, 6, 16),
      mainSumInsured = bd("300000"),
      hsdr17SumInsured = bd("300000"),
      ccb17SumInsured = bd("30000"),
      ccbh17SumInsured = null,
      premium = bd("2140.15"),
    )
    println("Policy: $policy")

    // when
    val saved = policyRepository.save(policy)

    // then
    println("Saved policy: $saved")
    saved shouldBeEqualTo policy.copy(id = saved.id)
    saved.id.shouldNotBeNull()
  }
}
