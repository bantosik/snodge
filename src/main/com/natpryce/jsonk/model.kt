package com.natpryce.jsonk

import java.math.BigDecimal

sealed class JsonElement

data class JsonObject(
    val properties: Map<String, JsonElement> = emptyMap()
) : JsonElement(), Map<String, JsonElement> by properties {
    
    constructor(properties: Iterable<Pair<String, JsonElement>>) :
        this(linkedMapOf<String, JsonElement>().apply { putAll(properties) })
    
    constructor(vararg properties: Pair<String, JsonElement>) :
        this(properties.toList())
}

fun JsonObject.withProperty(anotherProperty: Pair<String, JsonElement>) =
    copy(properties = this.properties + anotherProperty)

fun JsonObject.withProperties(vararg moreProperties: Pair<String, JsonElement>) =
    copy(properties = this.properties + moreProperties)

fun JsonObject.removeProperty(key: String) =
    copy(properties = this.properties - key)


data class JsonArray(
    val elements: List<JsonElement>
) : JsonElement(), List<JsonElement> by elements {
    constructor(vararg elements: JsonElement) : this(listOf(*elements))
}

fun JsonArray.append(anotherElement: JsonElement) =
    copy(elements = elements + anotherElement)

fun JsonArray.appendAll(moreElements: Iterable<JsonElement>) =
    copy(elements = elements + moreElements)

fun JsonArray.appendAll(vararg moreElements: JsonElement) =
    copy(elements = elements + moreElements)

fun JsonArray.remove(i: Int) =
    copy(elements = elements.toMutableList().also { it.removeAt(i) }.toList())

fun JsonArray.replace(i: Int, newElement: JsonElement) =
    copy(elements = elements.toMutableList().also { it[i] = newElement }.toList())

data class JsonNumber(val valueAsString: String) : JsonElement() {
    constructor(value: Int) : this(value.toString())
    constructor(value: Long) : this(value.toString())
    constructor(value: Double) : this(value.toString())
    
    fun toInt() = valueAsString.toInt()
    fun toLong() = valueAsString.toLong()
    fun toDouble() = valueAsString.toDouble()
    fun toBigDecimal() = BigDecimal(valueAsString)
}

data class JsonString(val value: String) : JsonElement()

data class JsonBoolean(val value: Boolean) : JsonElement()

object JsonNull : JsonElement()
