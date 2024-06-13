package com.example.ormik.policy

import com.example.ormik.IntegrationTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test

internal typealias bd = BigDecimal

class PolicyTest : IntegrationTest {

  @Autowired
  lateinit var policyRepository: PolicyRepository

  @Autowired
  lateinit var policyService: PolicyService

  @Test
  fun `saved policy has id`() {
    // given
    val policy = Fixture.policy()
    println("Policy: $policy")

    // when
    val saved = policyRepository.save(policy)

    // then
    println("Saved policy: $saved")
    saved shouldBeEqualTo policy.copy(id = saved.id)
    saved.id.shouldNotBeNull()
  }

  @Test
  fun `save two policies one by one`() {
    // given
    val policy1 = Fixture.policy()
    val policy2 = Fixture.policy()

    // when
    policyRepository.save(policy1)
    policyRepository.save(policy2)

    // then
    policyRepository.findAll() shouldHaveSize 2
  }

  @Test
  fun `save two policies at once`() {
    // given
    val policy1 = Fixture.policy()
    val policy2 = Fixture.policy()

    // when
    policyRepository.saveAll(listOf(policy1, policy2))

    // then
    policyRepository.findAll() shouldHaveSize 2
  }

  @Test
  fun `save two policies by service transactionally`() {
    // given
    val policy1 = Fixture.policy()
    val policy2 = Fixture.policy()

    // when
    policyService.saveAll(listOf(policy1, policy2))

    // then
    policyRepository.findAll() shouldHaveSize 2
  }
}

object Fixture {
  fun policy(
    id: Long? = null,
    holderParty: String = "Pan/Pani Holder",
    insuredParty: String = "Pan/Pani Insured",
    beneficiaryParty: String = "Pan/Pani Beneficiary",
    fromDate: LocalDate = LocalDate.of(2024, 6, 17),
    thruDate: LocalDate = LocalDate.of(2025, 6, 16),
    mainSumInsured: BigDecimal = bd("300000"),
    hsdr17SumInsured: BigDecimal? = bd("300000"),
    ccb17SumInsured: BigDecimal? = bd("30000"),
    ccbh17SumInsured: BigDecimal? = null,
    premium: BigDecimal = bd("2140.15"),
  ) = Policy(
    id = id,
    holderParty = holderParty,
    insuredParty = insuredParty,
    beneficiaryParty = beneficiaryParty,
    fromDate = fromDate,
    thruDate = thruDate,
    mainSumInsured = mainSumInsured,
    hsdr17SumInsured = hsdr17SumInsured,
    ccb17SumInsured = ccb17SumInsured,
    ccbh17SumInsured = ccbh17SumInsured,
    premium = premium,
  )
}
