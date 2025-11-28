package io.github.lemcoder.haystack.core.consts

import com.cactus.CactusModel
import io.github.lemcoder.koog.edge.cactus.CactusModels

val BaseLocalModel = CactusModels.Chat.Qwen3_0_6B
val BaseLocalModelAsCactusModel: CactusModel = CactusModel(
    created_at = "2025-08-24T00:04:55.975939+00:00",
    slug = "qwen3-0.6",
    download_url = "https://vlqqczxwyaodtcdmdmlw.supabase.co/storage/v1/object/public/cactus-models/qwen3-0.6.zip",
    size_mb = 394,
    supports_tool_calling = true,
    supports_vision = false,
    name = "Qwen 3 0.6B",
    isDownloaded = true,
    quantization = 8,
)

