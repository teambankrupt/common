package com.example.common.exceptions

import com.example.common.exceptions.forbidden.ForbiddenException
import com.example.common.exceptions.notfound.NotFoundException

sealed class Err(val throwable: Throwable) {
    data object GenericError :
        Err(RuntimeException("Generic Error, usually used as a placeholder for error."))

    sealed class ValidationErr(ex: Throwable, val instructionMsg: String) : Err(ex) {
        data class GenericValidationErr(val ex: Throwable, val instruction: String) :
            ValidationErr(ex, instruction)

        data class TextValidationErr(private val ex: Throwable, private val instruction: String) :
            ValidationErr(ex, instruction)

        data class EmailValidationErr(private val ex: Throwable, private val instruction: String) :
            ValidationErr(ex, instruction)
    }

    sealed class OperationErr(throwable: Throwable) : Err(throwable) {
        data class NonExistentErr(val id: Long) : OperationErr(NotFoundException("Item not found with id $id"))
        data object UnavailableErr : OperationErr(NotFoundException("Item not available."))
        data object ForbiddenErr : OperationErr(ForbiddenException("You are not allowed to perform this action."))
        data object ConstraintViolationErr :
            OperationErr(RuntimeException("Couldn't perform the action due to unresolved constraints."))
        data class NotAllowedErr(val instruction: String) :
            OperationErr(RuntimeException("Operation not allowed. $instruction"))
    }

    class UserErr(ex: Throwable) : Err(ex)

}

