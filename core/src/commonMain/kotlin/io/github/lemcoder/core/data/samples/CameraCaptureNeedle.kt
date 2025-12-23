package io.github.lemcoder.core.data.samples

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object CameraCaptureNeedle : SampleNeedle {
    @OptIn(ExperimentalUuidApi::class)
    override fun create() =
        Needle(
            id = Uuid.random().toString(),
            name = "Camera Capture Needle",
            description = "Launches the Android camera app using an Intent.",
            pythonCode =
                """
                from java import jclass
                from com.chaquo.python import Python

                # Use Application context (Chaquopy doesn't expose Activity)
                activity = Python.getPlatform().getApplication()

                Intent = jclass('android.content.Intent')
                MediaStore = jclass('android.provider.MediaStore')

                # Create camera intent
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                # REQUIRED when starting activities from Application context
                FLAG_NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(FLAG_NEW_TASK)

                # Launch camera
                activity.startActivity(intent)

                print("Camera intent launched")
                """
                    .trimIndent(),
            args = emptyList(),
            returnType = NeedleType.String,
        )
}
