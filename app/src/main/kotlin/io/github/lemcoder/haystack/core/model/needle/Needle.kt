package io.github.lemcoder.haystack.core.model.needle

data class Needle<T : NeedleType>(
    val id: String,
    val args: List<Arg>,
    val content: String,
) {
    data class Arg(
        val name: String,
        val type: NeedleType,
        val value: String,
    )
}
