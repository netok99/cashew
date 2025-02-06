package com.tranformation

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.leftNel
import arrow.core.nonEmptyListOf
import arrow.core.right

sealed interface InvalidField {
    val errors: NonEmptyList<String>
    val field: String
}

data class InvalidMcc(override val errors: NonEmptyList<String>) : InvalidField {
    override val field: String = "mcc"
}

data class InvalidMerchant(override val errors: NonEmptyList<String>) : InvalidField {
    override val field: String = "merchant"
}

data class InvalidTotalAmount(override val errors: NonEmptyList<String>) : InvalidField {
    override val field: String = "amount"
}

private const val MCC_SIZE_LENGTH = 4

fun String.validMcc(): Either<InvalidMcc, String> {
    val trimmed = trim()
    val regex = Regex(pattern = "^[0-9]{4}", options = setOf(RegexOption.IGNORE_CASE))
    return if (regex.matches(trimmed)) {
        trimmed.right()
    } else {
        InvalidMcc(nonEmptyListOf("Mcc: $trimmed is invalid")).left()
    }
}

fun String.validMerchant(): Either<InvalidMerchant, String> = trim().notBlank().mapLeft(::InvalidMerchant)

fun Double.validTotalAmount(): Either<InvalidTotalAmount, Double> =
    (if (this >= 0) right() else "Cannot be blank or less than 0".leftNel()).mapLeft(::InvalidTotalAmount)

fun String.notBlank(): EitherNel<String, String> =
    if (isNotBlank()) right() else "Cannot be blank".leftNel()

fun String.expectedSize(size: Int): EitherNel<String, String> =
    if (length >= size) right() else "Size is wrong, the right size is $size characters".leftNel()

fun <A : InvalidField> toInvalidField(
    transform: (NonEmptyList<String>) -> A
): (NonEmptyList<String>) -> NonEmptyList<A> = { nel -> nonEmptyListOf(transform(nel)) }
