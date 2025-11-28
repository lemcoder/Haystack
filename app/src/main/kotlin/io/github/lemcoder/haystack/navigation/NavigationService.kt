package io.github.lemcoder.haystack.navigation

import io.github.lemcoder.haystack.core.useCase.CheckIfModelDownloadedUseCase
import io.github.lemcoder.koog.edge.cactus.CactusLLMParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

interface NavigationService {
    val destinationFlow: StateFlow<Destination>

    fun navigateTo(destination: Destination)

    fun navigateBack()

    companion object {
        val Instance: NavigationService by lazy {
            NavigationServiceImpl()
        }
    }
}


private class NavigationServiceImpl(
    val checkModelDownloadedUseCase: CheckIfModelDownloadedUseCase = CheckIfModelDownloadedUseCase.create()
) : NavigationService {
    private val backStack by lazy {
        ArrayDeque<Destination>().apply {
            runBlocking {
                if (checkModelDownloadedUseCase()) {
                    add(Destination.Home)
                } else {
                    add(Destination.DownloadModel)
                }
            }
        }
    }
    private val _destinationFlow = MutableStateFlow(backStack.last())
    override val destinationFlow: StateFlow<Destination>
        get() = _destinationFlow.asStateFlow()


    override fun navigateTo(destination: Destination) {
        backStack.add(destination)
        _destinationFlow.update {
            destination
        }
    }

    override fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
            _destinationFlow.update {
                backStack.last()
            }
        }
    }
}