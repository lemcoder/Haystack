package io.github.lemcoder.haystack.core.model.needle

sealed interface NeedleType {
    object String : NeedleType
    object ByteArray : NeedleType
    object Number : NeedleType
}