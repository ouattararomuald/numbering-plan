package com.ouattararomuald.numbering

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CountryPlanTest {

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `invalid old phone number size should fail`() {
    assertThrows<IllegalStateException>("Should throw IllegalStateException") {
      CountryPlan.Builder()
        .setOldPhoneNumberSize(1)
        .setInternationalCallingCode("225")
        .setMigrationType(MigrationType.POSTFIX)
        .setDigitMapperPosition(Position.START)
        .setPrefixesMapper(
          mapOf(
            "07" to "07",
            "08" to "07",
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
  }

  @Test
  fun `invalid international calling code should fail`() {
    assertThrows<IllegalStateException>("Should throw IllegalStateException") {
      CountryPlan.Builder()
        .setOldPhoneNumberSize(8)
        .setInternationalCallingCode(" ")
        .setMigrationType(MigrationType.POSTFIX)
        .setDigitMapperPosition(Position.START)
        .setPrefixesMapper(
          mapOf(
            "01" to "01",
            "02" to "01",
            "03" to "01"
          )
        ).build()
    }

    assertThrows<IllegalStateException>("Should throw IllegalStateException") {
      CountryPlan.Builder()
        .setOldPhoneNumberSize(8)
        .setInternationalCallingCode("zaeazeaze")
        .setMigrationType(MigrationType.POSTFIX)
        .setDigitMapperPosition(Position.START)
        .setPrefixesMapper(
          mapOf(
            "07" to "07",
            "08" to "07",
          )
        ).build()
    }

    assertThrows<IllegalStateException>("Should throw IllegalStateException") {
      CountryPlan.Builder()
        .setOldPhoneNumberSize(8)
        .setInternationalCallingCode("2 2 5")
        .setMigrationType(MigrationType.POSTFIX)
        .setDigitMapperPosition(Position.START)
        .setPrefixesMapper(
          mapOf(
            "07" to "07",
            "08" to "07",
          )
        ).build()
    }

    assertThrows<IllegalStateException>("Should throw IllegalStateException") {
      CountryPlan.Builder()
        .setOldPhoneNumberSize(8)
        .setInternationalCallingCode("225")
        .setMigrationType(MigrationType.POSTFIX)
        .setDigitMapperPosition(Position.START)
        .setPrefixesMapper(emptyMap()).build()
    }
  }
}