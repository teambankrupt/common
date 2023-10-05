package com.example.common.exceptions

import com.example.common.utils.ExceptionUtil
import java.util.*

inline fun <reified T> Optional<T>.orDuckIt(id: Long): T =
    this.orElseThrow { ExceptionUtil.notFound(T::class.java.simpleName, id) }