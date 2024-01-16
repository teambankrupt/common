package com.example.common.validation

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.core.valid
import com.example.common.exceptions.Err

sealed interface ValidationScope {
    //	READ, WRITE, SEARCH, MODIFY
    data object Read : ValidationScope
    data object Write : ValidationScope
    data object Search : ValidationScope
    data object Modify : ValidationScope
}

interface ValidationV2<T> {
    fun apply(data: T, scope: ValidationScope): Either<Err.ValidationErr, T>
}

fun <T> genericValidation(
    message: String? = null,
    instruction: String = "",
    scopes: Set<ValidationScope>,
    exception: Throwable? = null,
    valid: (data: T) -> Boolean
): ValidationV2<T> =
    object : ValidationV2<T> {

        override fun apply(data: T, scope: ValidationScope): Either<Err.ValidationErr, T> =
            if ((scope !in scopes) || valid(data)) {
                data.right()
            } else {
                Err.ValidationErr
                    .GenericValidationErr(exception ?: RuntimeException(message ?: "$data is invalid"), instruction)
                    .left()
            }

    }

class EmailValidation() : ValidationV2<String> {
    override fun apply(data: String, scope: ValidationScope): Either<Err.ValidationErr, String> =
        if ("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$".toRegex().matches(data))
            data.right()
        else Err.ValidationErr.EmailValidationErr(
            RuntimeException("Invalid email address"),
            "Please input a valid email address"
        ).left()
}
