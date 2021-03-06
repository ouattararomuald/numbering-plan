package com.ouattararomuald.numbering

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NumberingPlanTest {

  private lateinit var numberingPlan: NumberingPlan

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `PREFIX migration should empty invalid numbers`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.PREFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf("08060709"),
        "userId-2" to listOf("06060709"),
        "userId-6" to listOf("zezae/03-060-701", "01020304"),
        "userId-3" to listOf("03060701"),
        "userId-4" to listOf(" 03 060 701 "),
        "userId-5" to listOf(" 03-060-701"),
        "userId-7" to listOf(")'.03-060-701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250708060709"),
        "userId-2" to listOf("002250506060709"),
        "userId-6" to listOf("", "002250101020304"),
        "userId-3" to listOf("002250103060701"),
        "userId-4" to listOf("002250103060701"),
        "userId-5" to listOf("002250103060701"),
        "userId-7" to listOf("")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `POSTFIX migration should empty invalid numbers`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.POSTFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf("08060709"),
        "userId-2" to listOf("06060709"),
        "userId-3" to listOf("03060701"),
        "userId-4" to listOf(" 03 060 701 "),
        "userId-5" to listOf(" 03-060-701"),
        "userId-6" to listOf("zezae/03-060-701"),
        "userId-7" to listOf(")'.03-060-701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250806070907"),
        "userId-2" to listOf("002250606070905"),
        "userId-3" to listOf("002250306070101"),
        "userId-4" to listOf("002250306070101"),
        "userId-5" to listOf("002250306070101"),
        "userId-6" to listOf(""),
        "userId-7" to listOf("")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `PREFIX migration of phone numbers without icc`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.PREFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)

    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf("08060709"),
        "userId-2" to listOf("06060709"),
        "userId-3" to listOf("03060701"),
        "userId-4" to listOf(" 03 060 701 "),
        "userId-5" to listOf(" 03-060-701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250708060709"),
        "userId-2" to listOf("002250506060709"),
        "userId-3" to listOf("002250103060701"),
        "userId-4" to listOf("002250103060701"),
        "userId-5" to listOf("002250103060701")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `POSTFIX migration of phone numbers without icc`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.POSTFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)

    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf("08060709"),
        "userId-2" to listOf("06060709"),
        "userId-3" to listOf("03060701"),
        "userId-4" to listOf(" 03 060 701 "),
        "userId-5" to listOf(" 03-060-701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250806070907"),
        "userId-2" to listOf("002250606070905"),
        "userId-3" to listOf("002250306070101"),
        "userId-4" to listOf("002250306070101"),
        "userId-5" to listOf("002250306070101")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `PREFIX migration with valid phone numbers`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.PREFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf("+22508060709"),
        "userId-2" to listOf("+22506060709"),
        "userId-3" to listOf("0022503060701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250708060709"),
        "userId-2" to listOf("002250506060709"),
        "userId-3" to listOf("002250103060701")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `POSTFIX migration with valid phone numbers`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.POSTFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf("+22508060709"),
        "userId-2" to listOf("+22506060709"),
        "userId-3" to listOf("0022503060701"),
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250806070907"),
        "userId-2" to listOf("002250606070905"),
        "userId-3" to listOf("002250306070101")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `PREFIX migration of phone numbers with invalid chars`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.PREFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf(" 00 22503 060 701 "),
        "userId-2" to listOf(" 00 225-03-060-701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250103060701"),
        "userId-2" to listOf("002250103060701")
      ),
      formattedPhoneNumbers
    )
  }

  @Test
  fun `POSTFIX migration of phone numbers with invalid chars`() {
    val ivoryCoastPlanFactory: CountryPlan = generateCountryPlan(MigrationType.POSTFIX)
    numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
    val formattedPhoneNumbers = numberingPlan.migrate(
      mapOf(
        "userId-1" to listOf(" 00 22503 060 701 "),
        "userId-2" to listOf(" 00 225-03-060-701")
      )
    )

    assertEquals(
      mapOf(
        "userId-1" to listOf("002250306070101"),
        "userId-2" to listOf("002250306070101")
      ),
      formattedPhoneNumbers
    )
  }

  private fun generateCountryPlan(migrationType: MigrationType) = CountryPlan.Builder()
    .setOldPhoneNumberSize(8)
    .setInternationalCallingCode("225")
    .setMigrationType(migrationType) // MigrationType.prefix | MigrationType.postfix
    .setDigitMapperPosition(Position.START)
    .setPrefixesMapper(
      mapOf(
        "07" to "07", // Orange
        "08" to "07", // eg: 08 XX XX XX => 07 08 XX XX XX (if MigrationType.prefix is used) => 08 XX XX XX 07 (if MigrationType.postfix is used)
        "09" to "07",
        "04" to "05", // MTN
        "05" to "05",
        "06" to "05",
        "01" to "01", // MOOV
        "02" to "01",
        "03" to "01"
      )
    ).build()
}