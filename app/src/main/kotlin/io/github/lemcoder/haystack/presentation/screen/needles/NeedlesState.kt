package io.github.lemcoder.haystack.presentation.screen.needles

import io.github.lemcoder.haystack.core.model.needle.Needle

data class NeedlesState(
    val needles: List<Needle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedNeedle: Needle? = null,
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val needleToDelete: Needle? = null,
)
