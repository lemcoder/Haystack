package io.github.lemcoder.lua

/**
 * Sample class for the lua module.
 * This demonstrates the basic structure of a class in the lua module.
 */
class SampleClass {
    /**
     * A sample property
     */
    val name: String = "Lua Module Sample"

    /**
     * A sample function that returns a greeting message
     */
    fun greet(): String {
        return "Hello from $name!"
    }

    /**
     * A sample function that executes a simple operation
     */
    fun execute(input: String): String {
        return "Processing: $input"
    }
}
