package com.ouattararomuald.numbering

import java.util.Objects

class CountryPlan private constructor(
  val oldPhoneNumberSize: Int,
  val digitMapperPosition: Position,
  val internationalCallingCode: String,
  val migrationType: MigrationType,
  val prefixesMapper: Map<String, String>
) {

  class Builder {
    @set:JvmSynthetic // Hide 'void' setter from Java
    var oldPhoneNumberSize: Int = 0

    @set:JvmSynthetic // Hide 'void' setter from Java
    var digitMapperPosition: Position = Position.START

    @set:JvmSynthetic // Hide 'void' setter from Java
    var internationalCallingCode: String? = null

    @set:JvmSynthetic // Hide 'void' setter from Java
    var migrationType: MigrationType = MigrationType.PREFIX

    @set:JvmSynthetic // Hide 'void' setter from Java
    var prefixesMapper: Map<String, String> = mutableMapOf()

    /**
     * Sets the actual size of phone numbers in the country you are targeting.
     *
     * @param oldPhoneNumberSize number of digits in a phone number.
     */
    fun setOldPhoneNumberSize(oldPhoneNumberSize: Int) = apply {
      this.oldPhoneNumberSize = oldPhoneNumberSize
    }

    /**
     * Sets the position where to look for the old prefix.
     *
     * Let's assume that your phone number is `08 XX XX XX` and you want to add `07` to phone numbers
     * **starting** with `08`. To indicates that the pattern you look in the old phone number is at
     * the beginning you use `Position.START`.
     *
     * If instead the the pattern is at the end you should use `Position.END`.
     *
     *
     * @param digitMapperPosition position of the prefix in old phone numbers.
     */
    fun setDigitMapperPosition(digitMapperPosition: Position) = apply {
      this.digitMapperPosition = digitMapperPosition
    }

    /**
     * Sets the International Calling Code that will apply to the phone numbers.
     *
     * The International Calling Code must be a number. You should not add the + symbol.
     *
     * @param internationalCallingCode International Calling Code.
     */
    fun setInternationalCallingCode(internationalCallingCode: String) = apply {
      this.internationalCallingCode = internationalCallingCode
    }

    /**
     * Determines where to add the new prefix. If it must be added before the old phone number
     * then use `MigrationType.prefix`. Otherwise you should use `MigrationType.postfix` that will
     * add the new digits after the old phone number.
     */
    fun setMigrationType(migrationType: MigrationType) = apply {
      this.migrationType = migrationType
    }

    /**
     * Sets the key-pair value of prefix whose key is the old prefix and value the new one.
     *
     * Let's say you passed `mapOf("08" to "07")` as input and your phone number is `08 XX XX XX`.
     * If the migration type is set to `MigrationType.prefix`, after the migration you new phone number
     * will be `07 08 XX XX XX`.
     *
     * This also means that the parameter `digitMapperPosition` is set to `Position.START`.
     * `Position.START` in this case indicates that we must look for "08" at the beginning of the
     * old phone number and add "07" to that phone number. We add it at the beginning because the
     * migration type is `MigrationType.prefix`.
     *
     * @param prefixesMapper key-value pairs of prefixes where the key is the old prefixes and the value the new prefixes.
     *
     * @see setDigitMapperPosition
     * @see setMigrationType
     */
    fun setPrefixesMapper(prefixesMapper: Map<String, String>) = apply {
      this.prefixesMapper = prefixesMapper
    }

    /**
     * Generates the plan.
     *
     * @throws IllegalStateException if the configuration is invalid.
     *
     * @return an instance of [CountryPlan].
     */
    fun build(): CountryPlan {
      if (oldPhoneNumberSize <= 1) {
        throw IllegalStateException("oldPhoneNumberSize must be greater than 1")
      }
      if (internationalCallingCode == null) {
        throw IllegalStateException("internationalCallingCode is null")
      }
      if (internationalCallingCode!!.isBlank()) {
        throw IllegalStateException("internationalCallingCode is blank")
      }
      if (internationalCallingCode!!.isNotNumber()) {
        throw IllegalStateException("internationalCallingCode is not a number")
      }
      if (prefixesMapper.isEmpty()) {
        throw IllegalStateException("prefixesMapper can not be empty")
      }

      return CountryPlan(
        oldPhoneNumberSize,
        digitMapperPosition,
        internationalCallingCode!!,
        migrationType,
        prefixesMapper
      )
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is CountryPlan && oldPhoneNumberSize == other.oldPhoneNumberSize
        && digitMapperPosition == other.digitMapperPosition
        && internationalCallingCode == other.internationalCallingCode
        && migrationType == other.migrationType
        && prefixesMapper == other.prefixesMapper
  }

  override fun hashCode(): Int = Objects.hash(
    oldPhoneNumberSize,
    digitMapperPosition, internationalCallingCode,
    migrationType, prefixesMapper
  )

  override fun toString(): String {
    return "CountryPlan(oldPhoneNumberSize=$oldPhoneNumberSize, digitToReplacePosition=$digitMapperPosition, internationalCallingCode=$internationalCallingCode, migrationType=$migrationType, prefixesMapper=$prefixesMapper)"
  }
}

private fun String?.isNotNumber(): Boolean {
  val numberPattern = "\\d+".toPattern()
  val matcher = numberPattern.matcher(this?.trim())
  return !matcher.matches()
}

@JvmSynthetic // Hide from Java callers who should use Builder.
fun CountryPlan(initializer: CountryPlan.Builder.() -> Unit): CountryPlan {
  return CountryPlan.Builder().apply(initializer).build()
}
