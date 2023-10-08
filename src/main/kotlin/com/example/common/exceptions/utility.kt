package com.example.common.exceptions

import arrow.core.Option
import arrow.core.toOption
import com.example.common.utils.ExceptionUtil
import java.util.*

inline fun <reified T> Optional<T>.orDuckIt(id: Long): T =
    this.orElseThrow { ExceptionUtil.notFound(T::class.java.simpleName, id) }

fun <T>Optional<T>.toArrow(): Option<T> = this.orElse(null).toOption()