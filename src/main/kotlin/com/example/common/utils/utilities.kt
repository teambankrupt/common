package com.example.common.utils

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

fun Map<String, Set<String>>.toHeaderMultiValueMap() =
	mapToMultiValueMap(this.mapValues { it.value.joinToString(",") })

fun mapToMultiValueMap(map: Map<String, String>): MultiValueMap<String, String> {
	val multiValueMap = LinkedMultiValueMap<String, String>()
	map.forEach { (key, value) ->
		multiValueMap.add(key, value)
	}
	return multiValueMap
}

inline fun <reified T> T?.requiredNotNull(key: String? = null) =
	this ?: throw ExceptionUtil.notAcceptable("${key ?: "Item"} can't be null")
