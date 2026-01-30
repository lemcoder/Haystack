package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.tools.ToolRegistry
import io.github.lemcoder.core.koog.NeedleToolAdapter
import io.github.lemcoder.core.model.needle.Needle

/**
 * Factory responsible for creating ToolRegistry instances from needles. Converts needles to Koog
 * tools and registers them in a ToolRegistry.
 */
class ToolRegistryFactory {

    /**
     * Creates a ToolRegistry with all provided needles as tools
     *
     * @param needles The list of needles to register as tools
     * @return ToolRegistry configured with needle tools
     */
    fun createToolRegistry(needles: List<Needle>): ToolRegistry {
        return ToolRegistry { needles.forEach { needle -> tool(NeedleToolAdapter(needle)) } }
    }
}
