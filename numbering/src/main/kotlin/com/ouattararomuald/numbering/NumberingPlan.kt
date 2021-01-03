package com.ouattararomuald.numbering

class NumberingPlan(val countryPlan: CountryPlan) {

  private val internationalCallingCodePattern =
    "^(\\+|0{2})?(?<icc>${countryPlan.internationalCallingCode})(?<phoneNumber>\\d{${countryPlan.oldPhoneNumberSize}})".toPattern()

  /**
   * Migrates the valid [phoneNumbers] to the new numbering plan.
   *
   * example:
   *
   * ```kotlin
   * val ivoryCoastPlanFactory: CountryPlan = CountryPlan.Builder()
   *   .setOldPhoneNumberSize(8)
   *   .setInternationalCallingCode("225")
   *   .setMigrationType(MigrationType.PREFIX)
   *   .setDigitMapperPosition(Position.START)
   *   .setPrefixesMapper(mapOf(
   *     "07" to "07",
   *     "08" to "07",
   *     "09" to "07",
   *     "04" to "05",
   *     "05" to "05",
   *     "06" to "05",
   *     "01" to "01",
   *     "02" to "01",
   *     "03" to "01"
   *   )).build()
   * val numberingPlan = NumberingPlan(ivoryCoastPlanFactory)
   * val formattedPhoneNumbers = numberingPlan.migrate(mapOf(
   *   "userId-1" to listOf("08060709"),
   *   "userId-2" to listOf("06060709"),
   *   "userId-3" to listOf("03060701"),
   *   "userId-4" to listOf(" 03 060 701 "),
   *   "userId-5" to listOf(" 03-060-701"),
   *   "userId-6" to listOf("zezae/03-060-701"),
   *   "userId-7" to listOf(")'.03-060-701")
   * ))
   * ```
   *
   * After the migration `formattedPhoneNumbers` will be equal to:
   * ```kotlin
   * mapOf(
   *   "userId-1" to listOf("002250806070907"),
   *   "userId-2" to listOf("002250606070905"),
   *   "userId-3" to listOf("002250306070101"),
   *   "userId-4" to listOf("002250306070101"),
   *   "userId-5" to listOf("002250306070101")
   * )
   * ```
   *
   * Invalid phone numbers will not pass the migration.
   *
   * @param phoneNumbers key-values list of phone numbers to migrate.
   * @param Key type of the key associated with each phone number.
   *
   * @throws MigrationException when fails to extract International Calling Code.
   * @throws MigrationException when fails to extract the Phone Number.
   *
   * @return the phone numbers whose migration was successful.
   */
  @Throws(MigrationException::class)
  fun <Key> migrate(phoneNumbers: Map<Key, List<String>>): Map<Key, List<String>> {
    val adaptedPhoneNumbers = phoneNumbers.map { it.key to it.value.map { number -> PhoneNumber(number) } }

    val outputPhoneNumbers: LinkedHashMap<Key, List<String>> = LinkedHashMap()
    adaptedPhoneNumbers.forEach { (key, givenPhoneNumbers) ->
      val cleanedPhoneNumbers = givenPhoneNumbers.map { givenPhoneNumber -> cleanPhoneNumber(givenPhoneNumber) }
      val formattedPhoneNumbers = mutableListOf<PhoneNumber>()
      cleanedPhoneNumbers.forEach { cleanedPhoneNumber ->
        if (cleanedPhoneNumber.isValidPhoneNumber() && cleanedPhoneNumber.isValidLength()) {
          formattedPhoneNumbers.add(formatPhoneNumber(cleanedPhoneNumber))
        } else {
          formattedPhoneNumbers.add(PhoneNumber(""))
        }
      }
      if (formattedPhoneNumbers.isNotEmpty()) {
        outputPhoneNumbers[key] = formattedPhoneNumbers.map { formattedPhoneNumber -> formattedPhoneNumber.value }
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
