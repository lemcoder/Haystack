package io.github.lemcoder.haystack.navigation

import io.github.lemcoder.core.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface NavigationService {
    val backStack: ArrayDeque<Destination>
    val destinationFlow: StateFlow<Destination>

    fun navigateTo(destination: Destination)

    fun navigateBack(): Boolean

    companion object {
        val Instance: NavigationService by lazy { NavigationServiceImpl() }
    }
}

private class NavigationServiceImpl() : NavigationService {
    private val tag = "NavigationService"
    override val backStack by lazy {
        ArrayDeque<Destination>().apply {
            add(Destination.Home)
        }
    }
    private val _destinationFlow = MutableStateFlow(backStack.last())
    override val destinationFlow: StateFlow<Destination>
        get() = _destinationFlow.asStateFlow()

    override fun navigateTo(destination: Destination) {
        Log.d(
            tag = tag,
            message = "-> ${backStack.toPrettyString()}"
        )
        backStack.add(destination)
        Log.d(
            tag = tag,
            message = "-> ${backStack.toPrettyString()}"
        )
        _destinationFlow.update { destination }
    }

    override fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            Log.d(
                tag = tag,
                message = "<- ${backStack.toPrettyString()}"
            )
            val last = backStack.removeLast()
            Log.d(
                tag = tag,
                message = "clearing ViewModelStore for $last"
            )
            last.viewModelStore.clear()
            Log.d(
                tag = tag,
                message = "<- ${backStack.toPrettyString()}"
            )
            _destinationFlow.update { backStack.last() }
            return true
        }
        return false
    }

    private fun ArrayDeque<Destination>.toPrettyString(): String {
        return "{${this.size}}${this.map { it::class.simpleName }}"
    }
}
