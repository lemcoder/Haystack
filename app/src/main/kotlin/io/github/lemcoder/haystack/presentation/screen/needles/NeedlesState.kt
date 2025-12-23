package io.github.lemcoder.haystack.presentation.screen.needles

import io.github.lemcoder.core.model.needle.Needle

data class NeedlesState(
    val needles: List<Needle> = emptyList(),
    val hiddenNeedleIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedNeedle: Needle? = null,
    val showCreateDialog: Boolean = false,
)
