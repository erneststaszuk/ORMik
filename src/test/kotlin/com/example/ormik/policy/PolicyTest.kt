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

internal typealias bd = BigDecimal

class PolicyTest : IntegrationTest {

  @Autowired
  lateinit var policyRepository: PolicyRepository

  @Autowired
  lateinit var policyService: PolicyService

  @Autowired
  lateinit var policyReportsRepository: PolicyReportsRepository

  @Test
  fun `saved policy has id`() {
    // given
    val policy = Fixture.policy()
    println("Policy: $policy")

    // when
    val saved = policyRepository.save(policy)

    // then
    println("Saved policy: $saved")
    saved shouldBeEqualTo policy.copy(id = saved.id, version = 1L)
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
  fun `save policy changes`() {
    // given
    val policy = Fixture.policy()
    val v1 = policyRepository.save(policy)
    println("Policy: $policy")
    println("Policy v1: $v1")

    // when
    val v2 = v1.copy(parties = v1.parties.copy(beneficiaryParty = "new beneficiary party"))
    val savedV2 = policyRepository.save(v2)
    println("v2: $v2")
    println("savedV2: $savedV2")

    // then
    savedV2 shouldBeEqualTo v2.copy(version = v2.version + 1)
    savedV2.version shouldBeEqualTo 2
  }
}

object Fixture {
  fun policy(
    id: UUID = UUID.randomUUID(),
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
    PolicyParties(
      holderParty = holderParty,
      insuredParty = insuredParty,
      beneficiaryParty = beneficiaryParty,
    ),
    fromDate = fromDate,
    thruDate = thruDate,
    mainSumInsured = mainSumInsured,
    hsdr17SumInsured = hsdr17SumInsured,
    ccb17SumInsured = ccb17SumInsured,
    ccbh17SumInsured = ccbh17SumInsured,
    premium = premium,
  )
}
