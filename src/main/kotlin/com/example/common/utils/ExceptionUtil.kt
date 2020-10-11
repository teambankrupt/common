package com.example.common.utils

import com.example.common.exceptions.exists.AlreadyExistsException
import com.example.common.exceptions.forbidden.ForbiddenException
import com.example.common.exceptions.invalid.InvalidException
import com.example.common.exceptions.notfound.NotFoundException

/**
 * Created by IntelliJ IDEA.
 * User: razzak
 * Date: 13/11/19
 * Time: 11:12 AM
 */

class ExceptionUtil {

    companion object {
        @JvmStatic
        val MSG_UNAUTHORIZED = "You're not authorized to access this resource."

        fun forbidden(message: String): ForbiddenException {
            return ForbiddenException(message)
        }

        fun notFound(message: String): NotFoundException {
            return NotFoundException(message)
        }

        fun notFound(entityName: String, id: Long): NotFoundException {
            return NotFoundException("Could not find $entityName with id: $id")
        }

        fun invalid(message: String): InvalidException {
            return InvalidException(message)
        }

        fun wtf(message: String): RuntimeException {
            return RuntimeException(message)
        }

        fun exists(message: String): AlreadyExistsException {
            return AlreadyExistsException(message)
        }

    }
}