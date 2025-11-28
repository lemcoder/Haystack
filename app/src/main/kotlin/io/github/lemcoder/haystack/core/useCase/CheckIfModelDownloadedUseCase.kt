package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.consts.BaseLocalModelAsCactusModel

interface CheckIfModelDownloadedUseCase {
    operator fun invoke(): Boolean

    companion object {
        fun create(): CheckIfModelDownloadedUseCase {
            return CheckIfModelDownloadedUseCaseImpl()
        }
    }
}

private class CheckIfModelDownloadedUseCaseImpl() : CheckIfModelDownloadedUseCase {
    override fun invoke(): Boolean = BaseLocalModelAsCactusModel.isDownloaded
}