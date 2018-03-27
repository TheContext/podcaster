package io.thecontext.ci

const val INDENT = "\t"

fun Iterable<String>.joinLines() = joinToString(separator = "\n")