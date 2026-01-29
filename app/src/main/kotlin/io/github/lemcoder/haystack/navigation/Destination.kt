package io.github.lemcoder.haystack.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import io.github.lemcoder.core.model.llm.ExecutorType

sealed interface Destination : ViewModelStoreOwner {
    data object Home : Destination {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    data object Needles : Destination {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    data class NeedleDetail(val needleId: String) : Destination {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    data object Settings : Destination {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    data object ExecutorSettings : Destination {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    data class ExecutorEdit(val executorType: ExecutorType?) : Destination {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }
}
