package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.utils.getFilesDirPath
import kotlinx.io.files.SystemFileSystem

interface CheckIfModelDownloadedUseCase {
    operator fun invoke(): Boolean

    companion object {
        fun create(): CheckIfModelDownloadedUseCase {
            return CheckIfModelDownloadedUseCaseImpl()
        }
    }
}

private class CheckIfModelDownloadedUseCaseImpl() : CheckIfModelDownloadedUseCase {
    override fun invoke(): Boolean {
        return SystemFileSystem.exists((kotlinx.io.files.Path(getFilesDirPath())))
    }
}
