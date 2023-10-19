package com.example.common.misc

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