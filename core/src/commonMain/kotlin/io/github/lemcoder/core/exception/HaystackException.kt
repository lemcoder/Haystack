package io.github.lemcoder.core.exception

sealed class HaystackException(override val message: String) : Exception(message)

class ExecutorNotSelectedException : HaystackException("No prompt executor selected.")
