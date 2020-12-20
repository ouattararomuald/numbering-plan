package com.ouattararomuald.numbering

class NumberingPlan(val countryPlan: CountryPlan) {

  private val internationalCallingCodePattern =
    "^(\\+|0{2})?(?<icc>${countryPlan.internationalCallingCode})(?<phoneNumber>\\d{${countryPlan.oldPhoneNumberSize}})".toPattern()

  /**
   * Migrates the given [phoneNumbers] to the new numbering plan.
   *
   * @param phoneNumbers key-values list of phone numbers to migrate.
   * @param Key type of key associated with each phone number.
   *
   * @throws MigrationException when fails to extract International Calling Code.
   * @throws MigrationException when fails to extract the Phone Number.
   *
   * @return the phone numbers whose migration was successful.
   */
  @Throws(MigrationException::class)
  fun <Key> migrate(phoneNumbers: Map<Key, PhoneNumber>): Map<Key, PhoneNumber> {
    val outputPhoneNumbers: MutableMap<Key, PhoneNumber> = mutableMapOf()
    phoneNumbers.forEach { (key, givenPhoneNumber) ->
      val cleanedPhoneNumber = cleanPhoneNumber(givenPhoneNumber)
      if (cleanedPhoneNumber.isValidPhoneNumber() && cleanedPhoneNumber.isValidLength()) {
        outputPhoneNumbers[key] = formatPhoneNumber(cleanedPhoneNumber)
      }
    }
    return outputPhoneNumbers
  }

  private fun formatPhoneNumber(phoneNumber: PhoneNumber): PhoneNumber {
    return if (phoneNumber.startsWithInternationalCallingCode()) {
      val icc = phoneNumber.extractICC()
      val number = phoneNumber.extractPhoneNumberWithoutICC().addNewPrefix()
      PhoneNumber("00$icc$number")
    } else {
      val icc = countryPlan.internationalCallingCode
      PhoneNumber("00$icc${phoneNumber.value.addNewPrefix()}")
    }
  }

  private fun String.addNewPrefix(): String { // TODO: Refactor
    val keys = countryPlan.prefixesMapper.keys

    var hasMatched = false
    var phone = this

    for (key in keys) {
      val prefix = countryPlan.prefixesMapper[key]
      phone = if (canRunPrefixMigration(key)) {
        hasMatched = true
        migratePrefix(prefix, phone)
      } else {
        phone
      }
      if (hasMatched) {
        break
      }
    }

    return phone
  }

  private fun String.canRunPrefixMigration(key: String): Boolean {
    return (countryPlan.digitMapperPosition == Position.START && this.startsWith(key))
        || (countryPlan.digitMapperPosition == Position.END && this.endsWith(key))
  }

  private fun migratePrefix(prefix: String?, phone: String): String {
    if (prefix == null) {
      return phone
    }

    return if (countryPlan.migrationType == MigrationType.PREFIX) {
      "$prefix$phone"
    } else {
      "$phone$prefix"
    }
  }

  private fun PhoneNumber.isValidLength(): Boolean {
    return value.length >= countryPlan.oldPhoneNumberSize
  }

  private fun PhoneNumber.startsWithInternationalCallingCode(): Boolean {
    return internationalCallingCodePattern.matcher(value).matches()
  }

  /**
   * Extracts the International Calling Code from the phone number.
   *
   * @throws MigrationException when fails to extract International Calling Code.
   *
   * @return the international calling code.
   */
  @Throws(MigrationException::class)
  private fun PhoneNumber.extractICC(): String {
    val matcher = internationalCallingCodePattern.matcher(value)
    if (matcher.find()) {
      return matcher.group(ICC_GROUP_NAME)
    }
    throw MigrationException("Failed to extract International Calling Code") // Should never happen
  }

  /**
   * Extracts the phone number.
   *
   * If the phone number has international calling code (ICC), this will return the number without
   * the ICC.
   *
   * @throws MigrationException when fails to extract the Phone Number.
   *
   * @return the phone number.
   */
  @Throws(MigrationException::class)
  private fun PhoneNumber.extractPhoneNumberWithoutICC(): String {
    val matcher = internationalCallingCodePattern.matcher(value)
    if (matcher.find()) {
      return matcher.group(PHONE_NUMBER_GROUP_NAME)
    }
    throw MigrationException("Failed to extract Phone Number") // Should never happen
  }

  /**
   * Removes invalid characters from phone numbers.
   *
   * @param phoneNumber the phone number to clean.
   *
   * @return the cleaned phone number.
   */
  private fun cleanPhoneNumber(phoneNumber: PhoneNumber): PhoneNumber {
    return PhoneNumber(phoneNumber.value.replace(SPACE_REGEX, EMPTY_STRING))
  }

  private fun PhoneNumber.isValidPhoneNumber(): Boolean {
    return PHONE_PATTERN_1_REGEX.matches(value) || PHONE_PATTERN_2_REGEX.matches(value) || PHONE_PATTERN_3_REGEX.matches(
      value
    )
  }

  companion object {
    private const val EMPTY_STRING = ""

    private const val ICC_GROUP_NAME = "icc"
    private const val PHONE_NUMBER_GROUP_NAME = "phoneNumber"

    private const val PHONE_PATTERN_1 = "^(?<phoneNumber>\\d+)"
    private const val PHONE_PATTERN_2 = "^\\+(?<phoneNumber>\\d+)"
    private const val PHONE_PATTERN_3 = "^0{2}(?<phoneNumber>\\d+)"

    private val PHONE_PATTERN_1_REGEX = PHONE_PATTERN_1.toRegex()
    private val PHONE_PATTERN_2_REGEX = PHONE_PATTERN_2.toRegex()
    private val PHONE_PATTERN_3_REGEX = PHONE_PATTERN_3.toRegex()

    private const val PHONE_PATTERN = "^(\\+|00)\\d+"
    private val PHONE_REGEX = PHONE_PATTERN.toRegex()
    private val SPACE_REGEX = "[\\s\\-]+".toRegex()
  }
}
