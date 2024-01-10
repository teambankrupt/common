package com.example.common.validation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.common.exceptions.Err

data class ValidatedObject<T>(val obj: T, val valid: Boolean)

interface ValidationV2<T> {
	fun apply(data: T): Either<Err.ValidationErr, T>
}

fun <T> genericValidation(
	message: String? = null,
	instruction: String = "",
	valid: (data: T) -> Boolean
): ValidationV2<T> =
	object : ValidationV2<T> {

		override fun apply(data: T): Either<Err.ValidationErr, T> =
			if (valid(data)) {
				data.right()
			} else {
				Err.ValidationErr
					.GenericValidationErr(RuntimeException(message ?: "$data is invalid"), instruction)
					.left()
			}

	}
class EmailValidation : ValidationV2<String> {
	override fun apply(data: String): Either<Err.ValidationErr, String> =
		if ("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$".toRegex().matches(data))
			data.right()
		else Err.ValidationErr.EmailValidationErr(
			RuntimeException("Invalid email address"),
			"Please input a valid email address"
		).left()
}
