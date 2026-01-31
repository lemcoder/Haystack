package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.tools.ToolRegistry
import io.github.lemcoder.core.koog.NeedleToolAdapter
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult

/**
 * Factory responsible for creating ToolRegistry instances from needles.
 *
 * Converts Haystack Needles into Koog tools and registers them in a ToolRegistry for use by AI
 * agents. Each needle is wrapped in a NeedleToolAdapter that handles execution and result
 * formatting.
 */
class ToolRegistryFactory {

    /**
     * Creates a ToolRegistry with all provided needles as tools
     *
     * @param needles The list of needles to register as tools
     * @param onNeedleResult Optional callback invoked when any needle executes
     * @return ToolRegistry configured with needle tools
     */
    fun createToolRegistry(
        needles: List<Needle>,
        onNeedleResult: ((Result<NeedleResult>) -> Unit)? = null,
    ): ToolRegistry {
        return ToolRegistry {
            needles.forEach { needle -> tool(NeedleToolAdapter(needle, onNeedleResult)) }
        }
    }
}
