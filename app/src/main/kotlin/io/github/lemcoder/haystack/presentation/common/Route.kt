package io.github.lemcoder.haystack.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService

@Composable
internal inline fun <reified T : Destination> Route(crossinline body: @Composable (T) -> Unit) {
    val navigationService = remember { NavigationService.Instance }
    val key = navigationService.findInBackStack<T>()!!
    CompositionLocalProvider(LocalViewModelStoreOwner provides key) {
        body(key)
    }
}

internal inline fun <reified T : Destination> NavigationService.findInBackStack(): T? {
    return backStack.firstOrNull { it is T } as? T
}