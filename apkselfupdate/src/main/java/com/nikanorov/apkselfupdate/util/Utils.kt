package com.nikanorov.apkselfupdate.util

import kotlin.reflect.KClass

fun Any.isOneOf(vararg states: KClass<out Any>): Boolean {
    return this::class in states
}