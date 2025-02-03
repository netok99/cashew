package com.tranformation

import arrow.core.Either
import arrow.core.Either.Companion.zipOrAccumulate
import arrow.core.EitherNel
import arrow.core.NonEmptyList
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

data class InvalidAccount(override val errors: NonEmptyList<String>) : InvalidField {
    override val field: String = "account"
}

data class InvalidMerchant(override val errors: NonEmptyList<String>) : InvalidField {
    override val field: String = "merchant"
}

data class InvalidTotalAmount(override val errors: NonEmptyList<String>) : InvalidField {
    override val field: String = "amount"
}

private const val MCC_SIZE_LENGTH = 4

fun String.validMcc(): EitherNel<InvalidMcc, String> {
    val trimmed = trim()
    return zipOrAccumulate(
        trimmed.notBlank(),
        trimmed.expectedSize(MCC_SIZE_LENGTH)
    ) { a, _ ->
        a
    }.mapLeft(toInvalidField(::InvalidMcc))
}

fun String.validAccount(): Either<InvalidAccount, String> =
    trim()
        .notBlank()
        .mapLeft(::InvalidAccount)

fun String.validMerchant(): Either<InvalidMerchant, String> =
    trim()
        .notBlank()
        .mapLeft(::InvalidMerchant)

fun Double.validTotalAmount(): Either<InvalidTotalAmount, Double> =
    (if (this >= 0) right() else "Cannot be blank or less than 0".leftNel())
        .mapLeft(::InvalidTotalAmount)

fun String.notBlank(): EitherNel<String, String> =
    if (isNotBlank()) right() else "Cannot be blank".leftNel()

fun String.expectedSize(size: Int): EitherNel<String, String> =
    if (length >= size) right() else "Size is wrong, the right size is $size characters".leftNel()

fun <A : InvalidField> toInvalidField(
    transform: (NonEmptyList<String>) -> A
): (NonEmptyList<String>) -> NonEmptyList<A> = { nel -> nonEmptyListOf(transform(nel)) }
