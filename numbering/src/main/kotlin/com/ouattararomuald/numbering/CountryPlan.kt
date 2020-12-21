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

    fun setOldPhoneNumberSize(oldPhoneNumberSize: Int) = apply {
      this.oldPhoneNumberSize = oldPhoneNumberSize
    }

    fun setDigitMapperPosition(digitMapperPosition: Position) = apply {
      this.digitMapperPosition = digitMapperPosition
    }

    fun setInternationalCallingCode(internationalCallingCode: String) = apply {
      this.internationalCallingCode = internationalCallingCode
    }

    fun setMigrationType(migrationType: MigrationType) = apply {
      this.migrationType = migrationType
    }

    fun setPrefixesMapper(prefixesMapper: Map<String, String>) = apply {
      this.prefixesMapper = prefixesMapper
    }

    fun build(): CountryPlan{
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
